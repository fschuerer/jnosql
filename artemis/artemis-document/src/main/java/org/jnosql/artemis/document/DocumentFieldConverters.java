/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.document;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnosql.artemis.AttributeConverter;
import org.jnosql.artemis.Embeddable;
import org.jnosql.artemis.reflection.FieldMapping;
import org.jnosql.artemis.reflection.GenericFieldMapping;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.document.Document;

class DocumentFieldConverters {

    static class DocumentFieldConverterFactory {

        private final EmbeddedFieldConverter embeddedFieldConverter = new EmbeddedFieldConverter();
        private final DefaultConverter defaultConverter = new DefaultConverter();
        private final CollectionEmbeddableConverter embeddableConverter = new CollectionEmbeddableConverter();
        private final SubEntityConverter subEntityConverter = new SubEntityConverter();
        private final MapEmbeddableConverter mapEmbeddableConverter = new MapEmbeddableConverter();

        DocumentFieldConverter get(FieldMapping field) {
            if (EMBEDDED.equals(field.getType())) {
                return embeddedFieldConverter;
            } else if (SUBENTITY.equals(field.getType())) {
                return subEntityConverter;
            } else if (isCollectionEmbeddable(field)) {
                return embeddableConverter;
            } else if (isMapEmbeddable(field)) {
                return mapEmbeddableConverter;
            } else {
                return defaultConverter;
            }
        }

        private boolean isCollectionEmbeddable(FieldMapping field) {
            return COLLECTION.equals(field.getType()) && ((GenericFieldMapping) field).isEmbeddable();
        }

        private boolean isMapEmbeddable(FieldMapping field) {
            if (MAP.equals(field.getType())) {
                Class type = DocumentFieldConverters.getMapValueType(field);
                return type.getAnnotation(Embeddable.class) != null;
            }
            return false;
        }
    }

    private static class SubEntityConverter implements DocumentFieldConverter {

        @Override
        public <T> void convert(T instance, List<Document> documents, Optional<Document> document,
                FieldMapping field, AbstractDocumentEntityConverter converter) {

            if (document.isPresent()) {
                Document sudDocument = document.get();
                Object value = sudDocument.get();
                if (value instanceof Map) {
                    Map map = (Map) value;
                    List<Document> embeddedDocument = new ArrayList<>();

                    for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                        embeddedDocument.add(Document.of(entry.getKey().toString(), entry.getValue()));
                    }
                    field.write(instance, converter.toEntity(field.getNativeField().getType(), embeddedDocument));

                } else {
                    field.write(instance, converter.toEntity(field.getNativeField().getType(),
                            sudDocument.get(new TypeReference<List<Document>>() {
                            })));
                }

            } else {
                field.write(instance, converter.toEntity(field.getNativeField().getType(), documents));
            }
        }
    }

    private static class EmbeddedFieldConverter implements DocumentFieldConverter {

        @Override
        public <T> void convert(T instance, List<Document> documents, Optional<Document> document,
                FieldMapping field, AbstractDocumentEntityConverter converter) {

            Field nativeField = field.getNativeField();
            Object subEntity = converter.toEntity(nativeField.getType(), documents);
            field.write(instance, subEntity);

        }
    }

    private static class DefaultConverter implements DocumentFieldConverter {

        @Override
        public <T> void convert(T instance, List<Document> documents, Optional<Document> document,
                FieldMapping field, AbstractDocumentEntityConverter converter) {
            Value value = document.get().getValue();

            Optional<Class<? extends AttributeConverter>> optionalConverter = field.getConverter();
            if (optionalConverter.isPresent()) {
                AttributeConverter attributeConverter = converter.getConverters().get(optionalConverter.get());
                Object attributeConverted = attributeConverter.convertToEntityAttribute(value.get());
                field.write(instance, field.getValue(Value.of(attributeConverted)));
            } else {
                field.write(instance, field.getValue(value));
            }
        }
    }

    private static class CollectionEmbeddableConverter implements DocumentFieldConverter {

        @Override
        public <T> void convert(T instance, List<Document> documents, Optional<Document> document,
                FieldMapping field, AbstractDocumentEntityConverter converter) {
            document.ifPresent(convertDocument(instance, field, converter));
        }

        private <T> Consumer<Document> convertDocument(T instance, FieldMapping field, AbstractDocumentEntityConverter converter) {
            return document -> {
                GenericFieldMapping genericField = (GenericFieldMapping) field;
                Collection collection = genericField.getCollectionInstance();
                List<List<Document>> embeddable = (List<List<Document>>) document.get();
                for (List<Document> documentList : embeddable) {
                    Object element = converter.toEntity(genericField.getElementType(), documentList);
                    collection.add(element);
                }
                field.write(instance, collection);
            };
        }
    }

    private static class MapEmbeddableConverter implements DocumentFieldConverter {

        @Override
        public <T> void convert(T instance, List<Document> documents, Optional<Document> document, FieldMapping field, AbstractDocumentEntityConverter converter) {
            document.ifPresent(convertDocument(instance, field, converter));
        }

        private <T> Consumer<Document> convertDocument(T instance, final FieldMapping field, final AbstractDocumentEntityConverter converter) {
            return (Document document) -> {
                Map<String, Object> map = createMapInstance(field);

                Object o = document.get();
                if (o instanceof Document) {
                    Document doc = (Document) o;
                    documentToMapEntry(field, map, doc, converter);
                } else if (o instanceof Collection) {
                    Collection<Document> c = (Collection<Document>) o;
                    c.forEach(doc -> documentToMapEntry(field, map, doc, converter));
                } else if (o instanceof Map) {
                    map.putAll((Map<String, Object>) o);
                }

                field.write(instance, map);
            };
        }

        private static Map<String, Object> createMapInstance(FieldMapping field) {
            try {
                Field nativeField = field.getNativeField();
                ParameterizedType pt = (ParameterizedType) nativeField.getGenericType();
                Object mapInstance = null;
                Class mapClass = (Class) pt.getRawType();
                if (mapClass.isInterface() || Modifier.isAbstract(mapClass.getModifiers())) {
                    if (SortedMap.class.isAssignableFrom(mapClass)) {
                        mapInstance = new TreeMap<String, Object>();
                    } else if (ConcurrentMap.class.isAssignableFrom(mapClass)) {
                        mapInstance = new ConcurrentHashMap<String, Object>();
                    } else {
                        mapInstance = new HashMap<String, Object>();
                    }
                } else {
                    mapInstance = mapClass.newInstance();
                }
                return (Map<String, Object>) mapInstance;
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(DocumentFieldConverters.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new HashMap<String, Object>();
        }

        private void documentToMapEntry(FieldMapping field, Map<String, Object> map, Document doc, AbstractDocumentEntityConverter converter) {
            String mapKey = doc.getName();
            Class mapValueType = getMapValueType(field);
            List<Document> values = (List<Document>) doc.getValue().get();
            Object mapValue = converter.toEntity(mapValueType, values);
            map.put(mapKey, mapValue);
        }
    }

    private static Class getMapValueType(FieldMapping field) {
        if (MAP.equals(field.getType())) {
            ParameterizedType pt = (ParameterizedType) field.getNativeField().getGenericType();
            Class type = (Class) pt.getActualTypeArguments()[1];
            return type;
        }
        return null;
    }
}
