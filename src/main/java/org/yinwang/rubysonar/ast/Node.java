package org.yinwang.rubysonar.ast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yinwang.rubysonar.Analyzer;
import org.yinwang.rubysonar.State;
import org.yinwang.rubysonar._;
import org.yinwang.rubysonar.types.Type;
import org.yinwang.rubysonar.types.UnionType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class Node implements java.io.Serializable {

    public String file;
    public int start = -1;
    public int end = -1;
    public String name;
    public String path;
    public String sha1;   // input source file sha
    public Node parent = null;


    public Node() {
    }


    public Node(String file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }


    public void setParent(Node parent) {
        this.parent = parent;
    }


    @NotNull
    public Node getAstRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getAstRoot();
    }


    public int length() {
        return end - start;
    }


    public void addChildren(@Nullable Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) {
                if (n != null) {
                    n.setParent(this);
                }
            }
        }
    }


    public void addChildren(@Nullable Collection<? extends Node> nodes) {
        if (nodes != null) {
            for (Node n : nodes) {
                if (n != null) {
                    n.setParent(this);
                }
            }
        }
    }


    public void setFile(String file) {
        this.file = file;
        this.sha1 = _.getSHA1(new File(file));
    }


    @NotNull
    public static Type transformExpr(@NotNull Node n, State s) {
        return n.transform(s);
    }


    @NotNull
    protected abstract Type transform(State s);


    public boolean isCall() {
        return this instanceof Call;
    }


    public boolean isModule() {
        return this instanceof Module;
    }


    public boolean isClassDef() {
        return false;
    }


    public boolean isFunctionDef() {
        return false;
    }


    public boolean isLambda() {
        return false;
    }


    public boolean isName() {
        return this instanceof Name;
    }


    public boolean isAssign() {
        return this instanceof Assign;
    }


    public boolean isBinOp() {
        return this instanceof BinOp;
    }


    @NotNull
    public BinOp asBinOp() {
        return (BinOp) this;
    }


    @NotNull
    public Call asCall() {
        return (Call) this;
    }


    @NotNull
    public Module asModule() {
        return (Module) this;
    }


    @NotNull
    public Class asClassDef() {
        return (Class) this;
    }


    @NotNull
    public Function asFunctionDef() {
        return (Function) this;
    }


    @NotNull
    public Name asName() {
        return (Name) this;
    }


    @NotNull
    public Assign asAssign() {
        return (Assign) this;
    }


    protected void addWarning(String msg) {
        Analyzer.self.putProblem(this, msg);
    }


    protected void addError(String msg) {
        Analyzer.self.putProblem(this, msg);
    }


    @NotNull
    protected Type resolveUnion(@NotNull Collection<? extends Node> nodes, State s) {
        Type result = Type.UNKNOWN;
        for (Node node : nodes) {
            Type nodeType = transformExpr(node, s);
            result = UnionType.union(result, nodeType);
        }
        return result;
    }


    @Nullable
    static protected List<Type> resolveList(@Nullable Collection<? extends Node> nodes, State s) {
        if (nodes == null) {
            return null;
        } else {
            List<Type> ret = new ArrayList<>();
            for (Node n : nodes) {
                ret.add(transformExpr(n, s));
            }
            return ret;
        }
    }


    // nodes are equal if they are from the same file and same starting point
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        } else {
            Node node = (Node) obj;
            String file = this.file;
            return (start == node.start &&
                    end == node.end &&
                    (file == null && node.file == null) ||
                    (file != null && node.file != null && file.equals(node.file)));
        }
    }


    public String toDisplay() {
        return "";
    }


    @NotNull
    @Override
    public String toString() {
        return "(node:" + file + ":" + name + ":" + start + ")";
    }

}
