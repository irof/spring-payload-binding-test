package com.example.demo;

import com.example.demo.todo.SearchResult;
import com.example.demo.todo.TodoStats;
import com.github.irof.test.spring_payload_binding.jackson2.EndpointPayloadTypes.PayloadType;
import com.github.irof.test.spring_payload_binding.jackson2.JsonBindingContractTestBase;
import com.github.irof.test.spring_payload_binding.jackson2.Variation;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {

    @Override
    protected List<Variation> variations(PayloadType payload) {
        // primitive を含む型は NULL variation で round-trip できない
        // ({"x": null} → x=0 → serialize → {"x": 0} となり source と差が出る)
        Class<?> raw = payload.getRawClass();
        if (raw == SearchResult.class || raw == TodoStats.class) {
            return List.of(Variation.SAMPLE, Variation.EMPTY);
        }
        return super.variations(payload);
    }
}
