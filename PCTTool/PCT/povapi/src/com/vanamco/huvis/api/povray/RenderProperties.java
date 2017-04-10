/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.api.povray;

import com.vanamco.huvis.modules.toolbox.projectRelated.NotifyProperties;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.spi.project.ProjectState;

/**
 *
 * @author ybrise
 */
public final class RenderProperties extends NotifyProperties {

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private int my_width;
    private int my_height;
    private int quality; // [1-11] see povray options
    private float antiAliasingOption;

    /**
     *
     * @param state
     */
    public RenderProperties(ProjectState state, PropertyChangeListener listener) {
        super(state);
        pcs.addPropertyChangeListener(listener);
        setWidth(400);
        setHeight(300);
        setQuality(9);
        setAntiAliasing(0.9f);
    }
    
    public void consolidate() {
        setWidth(Integer.parseInt(getProperty("W")));
        setHeight(Integer.parseInt(getProperty("H")));
        setQuality(Integer.parseInt(getProperty("Q")));
        setAntiAliasing(Float.parseFloat(getProperty("A")));
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return my_width;
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return my_height;
    }

    /**
     *
     * @return
     */
    public int getQuality() {
        return quality;
    }

    /**
     *
     * @return
     */
    public float getAntiAliasing() {
        return antiAliasingOption;
    }

    /**
     *
     * @param width
     */
    public void setWidth(int width) {
        setProperty("W", Integer.toString(width));
        this.my_width = width;
    }
    
    public final void pushWidth(int width) {
        pushProperty("W", Integer.toString(width));
        pcs.firePropertyChange("renderer-width", this.my_width, width);
        this.my_width = width;
    }

    /**
     *
     * @param height
     */
    public void setHeight(int height) {
        setProperty("H", Integer.toString(height));
        this.my_height = height;
    }
    
    public final void pushHeight(int height) {
        pushProperty("H", Integer.toString(height));
        pcs.firePropertyChange("renderer-height", this.my_height, height);
        this.my_height = height;
    }

    /**
     *
     * @param quality
     */
    public void setQuality(int quality) {
        setProperty("Q", Integer.toString(quality));
        this.quality = quality;
    }
    
    public final void pushQuality(int quality) {
        pushProperty("Q", Integer.toString(quality));
        pcs.firePropertyChange("renderer-quality", this.quality, quality);
        this.quality = quality;
    }

    /**
     *
     * @param value
     */
    public void setAntiAliasing(float value) {
        setProperty("A", Float.toString(value));
        this.antiAliasingOption = value;
    }
    
    public final void pushAntiAliasing(float value) {
        pushProperty("A", Float.toString(value));
        pcs.firePropertyChange("renderer-aliasing", this.antiAliasingOption, value);
        this.antiAliasingOption = value;
    }
}
