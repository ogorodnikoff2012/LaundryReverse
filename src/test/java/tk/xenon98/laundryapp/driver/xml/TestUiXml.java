
package tk.xenon98.laundryapp.driver.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class TestUiXml {

    @Test
    public void testLoadUi() throws IOException, JAXBException {
        final File file = new File("src/test/resources/ui.xml");
        try (final var reader = new FileReader(file)) {
            final JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
            final Hierarchy hier = ((JAXBElement<Hierarchy>) ctx.createUnmarshaller().unmarshal(reader))
                    .getValue();

            final var marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            final StringWriter writer = new StringWriter();
            marshaller.marshal(new ObjectFactory().createHierarchy(hier), writer);
            System.out.println(writer);
        }
    }

}