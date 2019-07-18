package com.pcauto.component;

import java.util.*;

public class BKTreeAlgo<T> {
    private final int radius;    // 模糊匹配的范围，如果值为0，就变成了精确匹配

    private Node root;
    private Metric<T> metric;

    public BKTreeAlgo(int radius, Metric<T> metric) {
        this.radius = radius;
        this.metric = metric;
    }

    public void add(T value) {
        if (root == null)
            root = new Node(value);
        else {
            root.add(value);
        }
    }

    public void addAll(Collection<? extends T> collection) {
        for (T val : collection) {
            add(val);
        }
    }

    public Set<T> search(T value) {
        Set<T> result = new HashSet<>();
        if (root != null)
            root.search(value, result);
        return result;
    }

    class Node {
        private T value;
        private Map<Integer, Node> childs;

        Node(T v) {
            this.value = v;
            this.childs = new HashMap<Integer, Node>();
        }

        void add(T value) {
            int distance = metric.getMetric(this.value, value);
            if (this.childs.containsKey(distance)) {
                this.childs.get(distance).add(value);
            } else {
                this.childs.put(distance, new Node(value));
            }
        }

        void search(T value, Set<T> resultSet) {
            int distance = BKTreeAlgo.this.metric.getMetric(this.value, value);

            if (distance <= radius) {    // 编辑距离在匹配范围，接入结果集
                resultSet.add(this.value);
            }

            for (int i = Math.max(distance - radius, 1); i <= distance + radius; i++) {
                Node ch = this.childs.get(i);
                if (ch != null)
                    ch.search(value, resultSet);
            }
        }
    }
}
