package com.collibra.identity.self.test;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

/**
 *
 */
@BuildParseTree
class AbcParser extends BaseParser<Object> {

    public Rule S() {
        return Sequence(
                Test(A(), 'c'),
                OneOrMore('a'),
                B(),
                TestNot(AnyOf("abc"))
        );
    }

    public Rule A() {
        return Sequence('a', Optional(A()), 'b');
    }

    public Rule B() {
        return Sequence('b', Optional(B()), 'c');
    }

}
