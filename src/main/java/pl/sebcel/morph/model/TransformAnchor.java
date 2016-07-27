package pl.sebcel.morph.model;

import javax.xml.bind.annotation.XmlElement;

public class TransformAnchor {

    @XmlElement
    private int originalX;
    
    @XmlElement
    private int originalY;

    @XmlElement
    private int targetX;
    
    @XmlElement
    private int targetY;

    public void setOriginalX(int originalX) {
        this.originalX = originalX;
    }

    public void setOriginalY(int originalY) {
        this.originalY = originalY;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public int getX(double phase) {
        int span = targetX - originalX;
        return this.originalX + (int) (span * phase);
    }

    public int getY(double phase) {
        int span = targetY - originalY;
        return this.originalY + (int) (span * phase);
    }

}