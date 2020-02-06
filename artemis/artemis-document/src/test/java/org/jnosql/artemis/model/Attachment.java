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

import org.jnosql.artemis.Column;
import org.jnosql.artemis.Embeddable;

@Embeddable
public class Attachment {

    @Column
    String content_type;

    @Column
    int revpos;

    @Column
    String digest;

    @Column
    long length;

    @Column
    boolean stub;

    public Attachment() {
    }

    public Attachment(String content_type, int revpos, String digest, long length, boolean stub) {
        this.content_type = content_type;
        this.revpos = revpos;
        this.digest = digest;
        this.length = length;
        this.stub = stub;
    }
    
    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public int getRevpos() {
        return revpos;
    }

    public void setRevpos(int revpos) {
        this.revpos = revpos;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }
}
