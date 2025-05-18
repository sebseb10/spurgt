package com.example.osmparsing.relations;

import java.io.Serializable;

/// WAITING TO BE ADOPTED BY FUTURE MULTIPOLYGON HANDLING AND ALSO HAS NO PARSING INTO IT
public class RelationMember implements Serializable {
    public enum RelationType {NODE, WAY, RELATION}

    private RelationType type;
    private String role;
    private long ref;
    private int index = -1;

    public RelationMember (RelationType type, String role, long ref) {
        this.type = type;
        this.role = role;
        this.ref = ref;
    }

    public RelationMember (RelationType type, String role, long ref, int index) {
        this.type = type;
        this.role = role;
        this.ref = ref;
        this.index = index;
    }
    public void setType (RelationType type) { this.type = type; }
    public RelationType getType() {
        return type;
    }
    public String getRole() {
        return role;
    }
    public long getRef() {
        return ref;
    }
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "RelationMember [type=" + type + ", role=" + role + ", ref=" + ref + ", index=" + index + "]";
    }
}