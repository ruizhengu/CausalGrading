package org.example;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class Digraph {
    private String graphname;
    private ArrayList<Node> nodes = new ArrayList<>();

    public static String STYLE_CLASS = "dashed";
    public static String STYLE_DATA = "dotted";

    public Digraph(String graphname) {
        this.graphname = graphname;
    }

    public Digraph addNode(String nodeID) {
        Node n = new Node(nodeID);
        n.graph = this;
        nodes.add(n);
        return this;
    }

    public void addNodeIfNotExists(String node) {
        if (!nodeExists(node)) {
            addNode(node);
        }
    }

    public void addNodeAndEdge(String startNode, String endNode) {
        addNodeAndEdge(startNode, endNode, null);
    }

    public void addNodeAndEdge(String startNode, String endNode, String style) {
        if (!nodeExists(startNode)) {
            addNode(startNode);
        }
        if (!nodeExists(endNode)) {
            addNode(endNode);
        }
        if (!edgeExists(startNode, endNode)) {
            link(startNode, endNode, style);
        }
    }

    public boolean nodeExists(String nodeID) {
        for (Node n : nodes) {
            if (n.nodeID.equals(nodeID)) {
                return true;
            }
        }
        return false;
    }

    public boolean edgeExists(String parentNodeID, String childNodeID) {
        Node parent = getNode(parentNodeID);
        ArrayList<Node> children = parent.getChildren();
        return children.contains(getNode(childNodeID));
    }

    public Node getNode(String nodeID) {
        for (Node n : nodes) {
            if (n.nodeID.equals(nodeID)) {
                return n;
            }
        }
        return null;
    }

    public Node addNode(String nodeID, String nodeName) {
        if (nodeExists(nodeID)) {
            System.out.println("Error: Node " + nodeID + " already exists.");
            System.exit(0);
        }
        Node n = new Node(nodeID, nodeName);
        n.graph = this;
        nodes.add(n);
        return n;
    }

    public Node link(String parentNodeID, String childNodeID, String style) {
        Node parent = getNode(parentNodeID);
        if (parent == null) {
            System.out.print("JavaGraph: Node " + parentNodeID + " does not exist.");
            System.exit(0);
        }
        Node child = getNode(childNodeID);
        if (child == null) {
            System.out.print("JavaGraph: Node " + childNodeID + " does not exist.");
            System.exit(0);
        }
        parent.style = style;
        parent.addChild(child);
        return child;
    }

    public void link(String parentNodeID, String childNodeID, String linkLabel, String style) {
        link(parentNodeID, childNodeID, style).linkLabel = linkLabel;
    }

    public void generate(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println("digraph " + graphname + " {");
            for (Node n : nodes) {
                if (n.hasName())
                    writer.println("\"" + n.nodeID + "\" [label=\"" + n.nodeName + "\"];");
                else
                    writer.println("\"" + n.nodeID + "\" [label=\"" + n.nodeID + "\"];");
            }
            for (Node n : nodes) {
                if (n.children.size() > 0) {
                    for (Node c : n.children) {
                        StringBuilder output = new StringBuilder("\"" + n.nodeID + "\" -> \"" + c.nodeID + "\"");
                        if (c.hasLabel()) {
                            output.append("[label=\"").append(c.linkLabel).append("\"]");
                        }
                        if (n.hasStyle()) {
                            output.append("[style=\"").append(n.style).append("\"]");
                        }
                        writer.println(output.append(";"));
                    }
                }
            }
            writer.println("}");
            writer.close();
            System.out.println("Tree generated");
        } catch (FileNotFoundException e) {
            System.out.println("JavaGraph: " + filename + " could not be written to.");
        } catch (UnsupportedEncodingException e) {
            System.out.print("JavaGraph: " + e.getMessage());
        }
    }

    class Node {
        private String nodeID;
        private String nodeName;
        private String linkLabel;
        private String style;
        private ArrayList<Node> children = new ArrayList<>();
        private Digraph graph;

        public Node(String nodeID) {
            this.nodeID = nodeID;
        }

        public void setLabel(String label) {
            this.linkLabel = label;
        }

        public Node(String nodeID, String nodeName) {
            this.nodeID = nodeID;
            this.nodeName = nodeName;
        }

        public Node setName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }

        public ArrayList<Node> getChildren() {
            return this.children;
        }

        public Node addChild(Node newChild) {
            if (newChild == null) {
                return null;
            }
            this.children.add(newChild);
            return newChild;
        }

        public Node addChild(Node newChild, String linkLabel) {
            if (newChild == null) {
                return null;
            }
            newChild.linkLabel = linkLabel;
            this.children.add(newChild);
            return newChild;
        }

        public boolean hasName() {
            return (nodeName != null);
        }

        public boolean hasLabel() {
            return (linkLabel != null);
        }

        public boolean hasStyle() {
            return (style != null);
        }
    }
}