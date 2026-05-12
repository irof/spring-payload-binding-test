package com.example.demo;

import com.example.testtool.JsonBindingContractTestBase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {

    @Override
    protected Mode defaultMode() {
        return Mode.VERIFY;
    }
}
