package com.collibra.identity.self.test;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

public class AtmostOneParser extends BaseParser<String> {
    /**
     * AP -> an *1(digit)
     * an -> alpha *(digit)
     * @return
     */
//    Rule start() {
//        return Sequence();
//    }

    public static void main(String[] args) {
//        AtmostOneParser parser = Parboiled.createParser(AtmostOneParser.class);
//
//        while (true) {
//            String input = new Scanner(System.in).nextLine();
//            if (StringUtils.isEmpty(input)) break;
//
//            ParsingResult<?> result = new ReportingParseRunner(parser.start()).run(input);
//
//            if (!result.parseErrors.isEmpty())
//                System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
//            else
//                System.out.println(printNodeTree(result) + '\n');
//
//        }
    }
}
