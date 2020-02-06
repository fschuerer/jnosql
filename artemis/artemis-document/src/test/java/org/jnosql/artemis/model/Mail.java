/*
 * Copyright 2019 fschuerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnosql.artemis.model;

import java.util.HashMap;
import java.util.Map;
import org.jnosql.artemis.Column;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

@Entity
public class Mail {

    @Id
    private String id;

    @Column
    private Map<String, Attachment> attachments = new HashMap<>();

    Mail() {
    }

    public Mail(String ids) {
        this.id = ids;
    }

    public String getId() {
        return id;
    }

    public void put(String name, Attachment a) {
        this.attachments.put(name, a);
    }

    public Map<String, Attachment> getAttachments() {
        return attachments;
    }

}
