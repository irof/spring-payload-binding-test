package com.example.demo;

import com.example.demo.todo.SearchResult;
import com.example.demo.todo.TodoStats;
import com.github.irof.test.spring_payload_binding.EngineVariation;
import com.github.irof.test.spring_payload_binding.JsonBindingContractTestBase;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SpringBoot3JsonBindingContractTest extends JsonBindingContractTestBase {

    @Override
    protected List<Variation> variations(PayloadTestContext ctx) {
        // primitive を含む型は NULL variation で round-trip できない
        // ({"x": null} → x=0 → serialize → {"x": 0} となり source と差が出る)
        Class<?> raw = ctx.rawClass();
        if (raw == SearchResult.class || raw == TodoStats.class) {
            return List.of(EngineVariation.SAMPLE, EngineVariation.EMPTY);
        }
        return super.variations(ctx);
    }
}
