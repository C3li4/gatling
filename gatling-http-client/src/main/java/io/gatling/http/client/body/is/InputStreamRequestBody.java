/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.http.client.body.is;

import io.gatling.http.client.body.RequestBody;
import io.gatling.http.client.body.RequestBodyBuilder;
import io.gatling.http.client.body.WritableContent;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.stream.FixedChunkedStream;
import java.io.IOException;
import java.io.InputStream;

public final class InputStreamRequestBody extends RequestBody.Base<InputStream> {

  private static class ConsumableInputStream extends InputStream {
    private final InputStream is;

    ConsumableInputStream(InputStream is) {
      this.is = is;
    }

    boolean consumed = false;

    public int read() throws IOException {
      consumed = true;
      return is.read();
    }

    @Override
    public int available() throws IOException {
      return is.available();
    }
  }

  public InputStreamRequestBody(InputStream stream, String contentType) {
    super(new ConsumableInputStream(stream), contentType);
  }

  @Override
  public WritableContent build(ByteBufAllocator alloc) {
    FixedChunkedStream chunkedStream = new FixedChunkedStream(content);
    return new WritableContent(chunkedStream, -1);
  }

  @Override
  public RequestBodyBuilder newBuilder() {
    return new InputStreamRequestBodyBuilder(content);
  }

  @Override
  public byte[] getBytes() {
    throw new UnsupportedOperationException("Can't read InputStream bytes without consuming it");
  }

  public boolean isConsumed() {
    return ((ConsumableInputStream) content).consumed;
  }

  @Override
  public String toString() {
    return "InputStreamRequestBody{" + "contentType='" + contentType + '\'' + ", content=???" + '}';
  }
}
