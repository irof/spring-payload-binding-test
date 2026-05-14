package com.example.demo;

import com.example.demo.todo.SearchResult;
import com.example.demo.todo.TodoList;
import com.example.demo.todo.TodoStats;
import com.github.irof.test.spring_payload_binding.JsonBindingContractTestBase;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SpringBoot3JsonBindingContractTest extends JsonBindingContractTestBase {

    @Override
    protected List<Variation> variations(PayloadTestContext payloadTestContext) {
        // primitive を含む型は NULL variation で round-trip できない
        // ({"x": null} → x=0 → serialize → {"x": 0} となり source と差が出る)
        Class<?> raw = payloadTestContext.rawClass();
        if (raw == SearchResult.class || raw == TodoStats.class) {
            return List.of(Variation.SAMPLE, Variation.EMPTY);
        }

        if (raw == TodoList.class) {
            return List.of(
                Variation.SAMPLE,
                Variation.SAMPLE.customMapping("scenario-hoge", configure -> configure
                        .type(String.class, "hoge")
                        .type(TodoList.Priority.class, "MEDIUM")),
                Variation.NULL,
                Variation.EMPTY
            );
        }

        return super.variations(payloadTestContext);
    }
}
