package com.collibra.identity.self.test;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

// A parser that takes lower case string and converts to CustomObject with upper case.
public class SimpleStringParser extends BaseParser<CustomString> {

    public Rule start() {
        return Sequence(anyChar(), EOI);
    }

    public Rule anyChar() {
        return Sequence(ZeroOrMore(CharRange('a', 'z')), push(new CustomString(match())));
    }

    public static void main(String[] args) {

        SimpleStringParser parser = Parboiled.createParser(SimpleStringParser.class);

        while (true) {
            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<?> result = new ReportingParseRunner(parser.start()).run(input);

            if (!result.parseErrors.isEmpty())
                System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
            else
                System.out.println(result.resultValue);
        }
    }
}

class CustomString {
    String input;
    CustomString(String input) {
        this.input = input.toUpperCase();
    }

    @Override
    public String toString() {
        return "CustomString{" +
                "input='" + input + '\'' +
                '}';
    }
}
