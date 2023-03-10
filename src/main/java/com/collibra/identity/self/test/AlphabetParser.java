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
public class AlphabetParser extends BaseParser<String> {

    public Rule start() {
        return Sequence(onlyAlphabets(),  EOI);
    }

    public Rule onlyAlphabets() {
        return Sequence(OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'))), !hasError() && push(match()));
    }

    public static void main(String[] args) {

        AlphabetParser parser = Parboiled.createParser(AlphabetParser.class);

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

