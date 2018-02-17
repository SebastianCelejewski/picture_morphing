package pl.sebcel.morph.utils;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import pl.sebcel.morph.model.TransformData;

public class FileOperations {

    public void saveProject(File file, TransformData project) {
        try {
            JAXBContext context = JAXBContext.newInstance(TransformData.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(project, file);
        } catch (Exception ex) {
            ex = getJAXBLinkedExceptionIfPresent(ex);
            throw new RuntimeException("Failed to save file: " + ex.getMessage(), ex);
        }
    }

    public TransformData loadProject(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(TransformData.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            TransformData project = (TransformData) unmarshaller.unmarshal(file);
            return project;
        } catch (Exception ex) {
            ex = getJAXBLinkedExceptionIfPresent(ex);
            throw new RuntimeException("Failed to load file: " + ex.getMessage(), ex);
        }
    }

    public BufferedImage loadImage(String path) {
		if (path == null) {
			return null;
		}
		try {
			return ImageIO.read(new File(path));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load image from file " + path + ": " + ex.getMessage(), ex);
		}
	}

    private Exception getJAXBLinkedExceptionIfPresent(Exception ex) {
        if (ex instanceof JAXBException) {
            JAXBException jex = (JAXBException) ex;
            if (jex.getLinkedException() != null) {
                return (Exception) jex.getLinkedException();
            }
        }
        return ex;
    }
}