package pl.sebcel.morph.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TransformData {

    @XmlElement
    private String sourceImagePath;

    @XmlElement
    private String targetImagePath;

    @XmlElement
    private List<TransformAnchor> anchors = new ArrayList<TransformAnchor>();

    public String getSourceImagePath() {
        return sourceImagePath;
    }

    public void setSourceImagePath(String sourceImagePath) {
        this.sourceImagePath = sourceImagePath;
    }

    public String getTargetImagePath() {
        return targetImagePath;
    }

    public void setTargetImagePath(String targetImagePath) {
        this.targetImagePath = targetImagePath;
    }

    public List<TransformAnchor> getAnchors() {
        return anchors;
    }
}