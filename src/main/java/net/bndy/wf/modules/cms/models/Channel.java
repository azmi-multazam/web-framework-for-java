package net.bndy.wf.modules.cms.models;

import net.bndy.wf.lib._BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name="cms_channel")
public class Channel extends _BaseEntity {

    private String name;
    private String path;
    @Enumerated(EnumType.ORDINAL)
    private BoType boType;
    private boolean isVisible;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BoType getBoType() {
        return boType;
    }

    public void setBoType(BoType boType) {
        this.boType = boType;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

}
