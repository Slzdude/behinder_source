package net.rebeyond.behinder.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class ReplacingInputStream extends FilterInputStream {
    LinkedList<Integer> inQueue = new LinkedList<>();
    LinkedList<Integer> outQueue = new LinkedList<>();
    final byte[] replacement;
    final byte[] search;

    public ReplacingInputStream(InputStream in, byte[] search2, byte[] replacement2) {
        super(in);
        this.search = search2;
        this.replacement = replacement2;
    }

    private boolean isMatchFound() {
        Iterator<Integer> inIter = this.inQueue.iterator();
        for (byte b : this.search) {
            if (!inIter.hasNext() || b != inIter.next().intValue()) {
                return false;
            }
        }
        return true;
    }

    private void readAhead() throws IOException {
        while (this.inQueue.size() < this.search.length) {
            int next = super.read();
            this.inQueue.offer(Integer.valueOf(next));
            if (next == -1) {
                return;
            }
        }
    }

    public int read() throws IOException {
        if (this.outQueue.isEmpty()) {
            readAhead();
            if (isMatchFound()) {
                for (int i = 0; i < this.search.length; i++) {
                    this.inQueue.remove();
                }
                for (byte b : this.replacement) {
                    this.outQueue.offer(Integer.valueOf(b));
                }
            } else {
                this.outQueue.add(this.inQueue.remove());
            }
        }
        return this.outQueue.remove().intValue();
    }
}
