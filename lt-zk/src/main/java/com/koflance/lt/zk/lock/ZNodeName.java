package com.koflance.lt.zk.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 代表一个零时有序节点，节点全路径的名字规则为 x-[sessionId]-[sequenceId]
 *
 * 这里使用了sessionId作为guid
 *
 */
class ZNodeName implements Comparable<ZNodeName> {
    /*节点的全路径名，例如x-[sessionId]-0000000010*/
    private final String name;
    /*前缀，例如x-[sessionId]*/
    private String prefix;
    /*序号，例如10*/
    private int sequence = -1;
    private static final Logger LOG = LoggerFactory.getLogger(ZNodeName.class);

    /**
     * 零时节点的全路径名字
     * 例如：x-[sessionId]-0000000010
     * @param name
     */
    public ZNodeName(String name) {
        if (name == null) {
            throw new NullPointerException("id cannot be null");
        }
        this.name = name;
        this.prefix = name;
        int idx = name.lastIndexOf('-');
        if (idx >= 0) {
            this.prefix = name.substring(0, idx);
            try {
                this.sequence = Integer.parseInt(name.substring(idx + 1));
                // If an exception occurred we misdetected a sequence suffix,
                // so return -1.
            } catch (NumberFormatException e) {
                LOG.info("Number format exception for " + idx, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.info("Array out of bounds for " + idx, e);
            }
        }
    }

    @Override
    public String toString() {
        return name.toString();
    }

    /**
     * 只比较前缀
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZNodeName sequence = (ZNodeName) o;

        if (!name.equals(sequence.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + 37;
    }

    /**
     * 先比较前缀x-[sessionId]，一样再比较sequence大小，如果没有sequence，则用name比较
     *
     * @param that
     * @return
     */
    public int compareTo(ZNodeName that) {
        int answer = this.prefix.compareTo(that.prefix);
        if (answer == 0) {
            int s1 = this.sequence;
            int s2 = that.sequence;
            if (s1 == -1 && s2 == -1) {
                return this.name.compareTo(that.name);
            }
            answer = s1 == -1 ? 1 : s2 == -1 ? -1 : s1 - s2;
        }
        return answer;
    }

    /**
     * Returns the name of the znode
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the sequence number
     */
    public int getZNodeName() {
        return sequence;
    }

    /**
     * Returns the text prefix before the sequence number
     */
    public String getPrefix() {
        return prefix;
    }

    public static void main(String[] args) {
        String[] names = {"x-216181935346614273-0000000003","x-216181935346614274-0000000004","x-216181935346614272-0000000002", "x-144124343741251585-0000000001", "y", "x", "x-123-3", "x-123-5", "x-232-11", "x-212-1" };
        SortedSet<ZNodeName> nodeNames = new TreeSet<ZNodeName>();
        for (String name : names) {
            nodeNames.add(new ZNodeName(name));
        }
        for (ZNodeName nodeName : nodeNames) {
            String name = nodeName.getName();
            System.out.println(name);
        }
    }
}