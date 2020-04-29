/*
 * Copyright © 2020 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.send;


import net.luminis.quic.frame.QuicFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SendRequestQueue {

    private List<SendRequest> requestQueue = Collections.synchronizedList(new ArrayList<>());


    public void addRequest(QuicFrame fixedFrame) {
        requestQueue.add(new SendRequest(fixedFrame.getBytes().length, actualMaxSize -> fixedFrame));
    }

    public void addAckRequest(int maxDelay) {
    }

    /**
     *
     * @param frameSupplier
     * @param estimatedSize   The minimum size of the frame that the supplier can produce. When the supplier is
     *                        requested to produce a frame of that size, it must return a frame of the size or smaller.
     *                        This leaves room for the caller to handle uncertainty of how large the frame will be,
     *                        for example due to a var-length int value that may be larger at the moment the frame
     *                        must be produced than the actual value when the request is queued.
     */
    public void addRequest(Function<Integer, QuicFrame> frameSupplier, int estimatedSize) {
        requestQueue.add(new SendRequest(estimatedSize, frameSupplier));
    }

    public Function<Integer, QuicFrame> next(int maxFrameLength) {
        if (maxFrameLength < 1) {  // Minimum frame size is 1 indeed: a frame may be just a type field.
            // Forget it
            return null;
        }
        for (int i = 0; i < requestQueue.size(); i++) {
            if (requestQueue.get(i).estimatedSize <= maxFrameLength) {
                return requestQueue.remove(i).frameSupplier;
            }
        }
        // Couldn't find one.
        return null;
    }

    static class SendRequest {
        int estimatedSize;
        Function<Integer, QuicFrame> frameSupplier;

        public SendRequest(int estimatedSize, Function<Integer, QuicFrame> frameSupplier) {
            this.estimatedSize = estimatedSize;
            this.frameSupplier = frameSupplier;
        }
    }
}

