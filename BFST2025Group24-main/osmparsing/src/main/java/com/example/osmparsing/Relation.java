package com.example.osmparsing;

import com.example.osmparsing.MVC.Model;
import com.example.osmparsing.relations.RelationMember;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.example.osmparsing.MVC.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// WAITING TO BE ADOPTED BY FUTURE MULTIPOLYGON HANDLING AND ALSO HAS NO PARSING INTO IT
public class Relation extends OSMElement implements Serializable {
    private long id;
    private List <RelationMember> members;
    private Map<String, String> tags;
    private Model model;

    public Relation(long id,List <RelationMember> members, Map<String, String> tags) {
        super(id);
        this.id = id;
        this.members = new ArrayList<>(members);
        this.tags = tags;
    }
    public long getId() {
        return id;
    }
    public List <RelationMember> getMembers() {
        return members;
    }
    public Map<String, String> getTags() {
        return tags;
    }
    @Override
    public String toString() {
        return "Relation[" + "id=" + id + ":members=" + members + ":tags=" + tags + "]";
    }

    public void drawRelation(GraphicsContext gc, Model model) {
        List <RelationMember> outerways = new ArrayList<>();
        List<RelationMember> innerways = new ArrayList<>();

        for (RelationMember member : members) {
            if (member.getRole().equals("outer")){
                outerways.add(member);
            } else if(member.getRole().equals("inner")){
                innerways.add(member);
            }
        }

        if(!outerways.isEmpty()){
            long id = outerways.get(0).getRef();
            Way outerWay = model.getWayMapping().get(id);
            if (outerWay != null) {
                outerWay.drawEle(gc);
            }
        }
    }

    @Override
    public void drawEle(GraphicsContext gc) {


        /*
        color = Color.LIGHTGRAY;
        int nPoints = coords.length / 2;
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];

        for (int i = 0 ; i < nPoints ; ++i) {
            xPoints[i] = coords[2 * i];
            yPoints[i] = coords[2 * i + 1];
        }

        gc.setFill(color);
        gc.setStroke(this.color);
        gc.fillPolygon(xPoints, yPoints, nPoints);

         */

    }
}