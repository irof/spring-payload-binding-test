package com.example.demo;

import com.example.demo.todo.SearchResult;
import com.example.demo.todo.TodoStats;
import com.example.testtool.EndpointPayloadTypes.Direction;
import com.example.testtool.EndpointPayloadTypes.PayloadType;
import com.example.testtool.JsonBindingContractTestBase;
import com.example.testtool.Variation;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {

    @Override
    protected Mode defaultMode() {
        return Mode.VERIFY;
    }

    @Override
    protected boolean shouldRun(PayloadType payload, Variation variation) {
        // primitive を含む型は NULL response variation で round-trip できない
        // ({"x": null} → x=0 → serialize → {"x": 0} となり source と差が出る)
        if (variation == Variation.NULL && payload.direction() == Direction.RESPONSE) {
            Class<?> raw = payload.type().getRawClass();
            if (raw == SearchResult.class || raw == TodoStats.class) return false;
        }
        return true;
    }
}
