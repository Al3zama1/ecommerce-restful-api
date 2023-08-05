package com.abranlezama.ecommercerestfulapi.hazelcast;

import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class HttpResponseCompactSerializer implements CompactSerializer<HttpResponse> {

    @Override
    public HttpResponse read(CompactReader in) {
        return HttpResponse.builder()
                .message(in.readString("message"))
                .timeStamp(in.readString("timestamp"))
                .status(in.readString("status"))
                .statusCode(in.readInt32("statusCode"))
                .errorMessage(in.readString("errorMessage"))
                .result(in.readCompact("result"))
                .build();
    }

    @Override
    public void write(CompactWriter out, HttpResponse httpResponse) {
        out.writeString("message", httpResponse.getMessage());
        out.writeString("status", httpResponse.getStatus());
        out.writeInt32("statusCode", httpResponse.getStatusCode());
        out.writeString("timestamp", httpResponse.getTimeStamp());
        out.writeString("errorMessage", httpResponse.getErrorMessage());
        out.writeCompact("result", httpResponse.getResult());
    }

    @Override
    public String getTypeName() {
        return "httpResponse";
    }

    @Override
    public Class<HttpResponse> getCompactClass() {
        return HttpResponse.class;

    }
}
