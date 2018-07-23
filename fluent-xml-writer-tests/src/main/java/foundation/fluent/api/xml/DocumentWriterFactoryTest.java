/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2018, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.fluent.api.xml;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.util.function.Consumer;

import static foundation.fluent.api.xml.DocumentWriterFactory.*;
import static org.testng.Assert.assertEquals;

public class DocumentWriterFactoryTest {

    private Object[] requirement(Consumer<DocumentWriter> actual, String expected) {
        return new Object[] {actual, expected};
    }

    @DataProvider
    public Object[][] data() {
        return new Object[][] {
                requirement(
                        w -> w.tag("element").end(),
                        "<element/>"
                ),

                requirement(
                        w -> w.version(1.0).tag("element").end(),
                        "<?xml version='1.0'?><element/>"
                ),

                requirement(
                        w -> w.encoding("UTF-8").tag("element").end(),
                        "<?xml encoding='UTF-8'?><element/>"
                ),

                requirement(
                        w -> w.version(1.0).encoding("UTF-8").tag("element").close(),
                        "<?xml version='1.0' encoding='UTF-8'?><element/>"
                ),

                requirement(
                        w -> w.version(1.0).encoding("UTF-8")
                                .tag("element").attribute("a", "b").xmlns("http://my/uri")
                                .text("aha")
                                .cdata("&uuu")
                                .cdata(" f")
                                .end(),
                        "<?xml version='1.0' encoding='UTF-8'?><element a='b' xmlns='http://my/uri'>aha<!CDATA[&uuu f]></element>"
                ),

                requirement(
                        w -> {
                            RootElementWriter tag = w.tag("tag");
                            tag.tag("one").attribute("a", "u").text("1");
                            tag.tag("two").attribute("b", "v").text("2");
                            tag.close();
                        },
                        "<tag><one a='u'>1</one><two b='v'>2</two></tag>"
                )
        };
    }

    @Test(dataProvider = "data")
    public void test(Consumer<DocumentWriter> actual, String expected) {
        StringWriter writer = new StringWriter();
        actual.accept(document(writer));
        assertEquals(writer.toString(), expected);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "No root element created.")
    public void testVersionEncodingAndNoTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).version(1.0).encoding("UTF-8").close();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Trying to output second root.")
    public void testVersionEncodingAndTwoTopLevelElement() {
        StringWriter writer = new StringWriter();
        DocumentWriter documentWriter = document(writer).version(1.0).encoding("UTF-8");
        documentWriter.tag("root").end();
        documentWriter.tag("root").close();
    }

}
