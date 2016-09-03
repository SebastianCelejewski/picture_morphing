package pl.sebcel.morph.model;

import javax.xml.bind.annotation.XmlElement;

public class TransformAnchor {

    @XmlElement
    private double originalX;
    
    @XmlElement
    private double originalY;

    @XmlElement
    private double targetX;
    
    @XmlElement
    private double targetY;

    public void setOriginalX(double originalX) {
        this.originalX = originalX;
    }

    public void setOriginalY(double originalY) {
        this.originalY = originalY;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

    public double getX(double phase) {
    	double span = targetX - originalX;
        return this.originalX + span * phase;
    }

    public double getY(double phase) {
    	double span = targetY - originalY;
        return this.originalY + span * phase;
    }

}