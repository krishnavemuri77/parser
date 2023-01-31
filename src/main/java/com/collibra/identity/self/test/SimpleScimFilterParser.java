package com.collibra.identity.self.test;

import org.parboiled.*;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;


import java.util.Scanner;


/**
 * FILTER    = attrExp
 * attrExp   = (attrPath "pr") / (attrPath compareOp compValue)
 * attrPath  = [URI ":"] attrName *1subAttr
 * compValue = false / null / true / number / string
 * compareOp = "eq" / "ne" / "co" / "sw" / "ew" / "gt" / "lt" / "ge" / "le"
 * ATTRNAME  = ALPHA *(nameChar)
 * nameChar  = "-" / "_" / DIGIT / ALPHA
 * subAttr   = "." ATTRNAME
 * <p>
 * Eg :
 * filter=title pr
 * userType eq "Intern"
 */
public class SimpleScimFilterParser extends BaseParser<Object> {

    // * FILTER    = attrExp
    Rule filter() {
        return Sequence(attrExp(), EOI);
    }

    //attrExp   = (attrPath "pr") / (attrPath compareOp compValue)
    Rule attrExp() {
        return FirstOf(
                Sequence(
                        attrPath(),
                        space(),
                        IgnoreCase("pr"),
                        push(new AttributePresentExpression(pop()))
                ),
                Sequence(
                        attrPath(),
                        space(),
                        compareOp(),
                        space(),
                        compValue(),
                        push(new AttributeEqualsExpression(pop(2), pop(1), pop()))
                )
        );
    }

    Rule compareOp() {
        return Sequence(
                FirstOf(
                        IgnoreCase("eq"),
                        IgnoreCase("ne"),
                        IgnoreCase("co"),
                        IgnoreCase("sw"),
                        IgnoreCase("ew"),
                        IgnoreCase("gt"),
                        IgnoreCase("lt"),
                        IgnoreCase("ge"),
                        IgnoreCase("le")
                ),
                push(CompareOperator.valueOf(match().toLowerCase()))
        );
    }

    Rule compValue() {
        return Sequence(
                FirstOf(
                        IgnoreCase("false"),
                        "null",
                        IgnoreCase("true"),
                        number(),
                        string()
                ),
                push(match()));
    }

    // * attrPath  = [URI ":"] attrName *1subAttr
    Rule attrPath() {
        return Sequence(
                attrName(),
                Optional(subAttr()),
                push(match().isEmpty() ? null : new SubAttr(pop())),
                push(new AttributePath(pop(1), pop()))
        );
    }

    // * ATTRNAME  = ALPHA *(nameChar)
    Rule attrName() {
        return Sequence(
                alpha(),
                ZeroOrMore(
                        nameChar()
                ),
                push(new AttributeName(pop(), match()))
        );
    }

    Rule nameChar() {
        return FirstOf(
                Ch('-'),
                Ch('_'),
                CharRange('0', '9'),
                CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    // * subAttr   = "." ATTRNAME
    Rule subAttr() {
        return Sequence(".", attrName());
    }

    // pushes an alphabet on to the stack.
    public Rule alpha() {
        return Sequence(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')), push(match()));
    }

    Rule number() {
        return OneOrMore(CharRange('0', '9'));
    }

    public Rule string() {
        return Sequence('"', ZeroOrMore(FirstOf(
                        CharRange('a', 'z'),
                        CharRange('A', 'Z'),
                        CharRange('0', '9'),
                        AnyOf("!#$%&'()*+, -./:;<=>?@[\\]^_`{|}~"))),
                '"');
    }

    public Rule space() {
        return ZeroOrMore(" ");
    }

    public static void main(String[] args) {

        SimpleScimFilterParser parser = Parboiled.createParser(SimpleScimFilterParser.class);

        while (true) {
            System.out.print("Enter a filter expression (single RETURN to exit)!\n");

            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<?> result = new ReportingParseRunner(parser.filter()).run(input);

            if (!result.parseErrors.isEmpty())
                System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
            else {
                System.out.println(((Element) result.resultValue).getValue());
                System.out.println(result.resultValue);
            }
        }
    }

}

class AttributePresentExpression implements Element {

    private AttributePath attributePath;

    AttributePresentExpression(Object path) {
        this.attributePath = (AttributePath) path;
    }

    public String getValue() {
        return "( " + attributePath.getValue() + " pr " + ")";
    }

    @Override
    public String toString() {
        return "AttributePresentExpression{" +
                "attributePath=" + attributePath +
                '}';
    }
}

class AttributeEqualsExpression implements Element {
    private AttributePath attributePath;
    private Enum compareOperator;
    private String compareValue;

    AttributeEqualsExpression(Object path, Object compareOperator, Object value) {
        this.attributePath = (AttributePath) path;
        this.compareOperator = (CompareOperator) compareOperator;
        this.compareValue = (String) value;
    }

    public String getValue() {
        return "( " + attributePath.getValue() + " " + compareOperator + " " + compareValue + " )";
    }

    @Override
    public String toString() {
        return "AttributeEqualsExpression{" +
                "attributePath=" + attributePath +
                ", compareOperator=" + compareOperator +
                ", compareValue='" + compareValue + '\'' +
                '}';
    }
}

class AttributeName implements Element{

    private String alpha;
    private String nameChar;

    AttributeName(Object alpha, Object nameChar) {
        this.alpha = (String) alpha;
        if (!((String) nameChar).isEmpty()) {
            this.nameChar = (String) nameChar;
        }
    }

    public String getValue() {
        if(nameChar == null)
            return alpha;
        return alpha + nameChar;
    }

    @Override
    public String toString() {
        return "AttributeName{" +
                "alpha='" + alpha + '\'' +
                ", nameChar='" + nameChar + '\'' +
                '}';
    }
}

class SubAttr implements Element {

    AttributeName attributeName;

    SubAttr(Object attributeName) {
        if (attributeName != null) {
            this.attributeName = (AttributeName) attributeName;
        }
    }

    public String getValue() {
        return attributeName == null ? "" : "."+ attributeName.getValue();
    }

    @Override
    public String toString() {
        return "SubAttr{" +
                "attributeName=" + attributeName +
                '}';
    }
}

class AttributePath implements Element {

    private AttributeName attributeName;
    private SubAttr subAttr;

    public AttributePath(Object attributeName, Object subAttr) {
        this.attributeName = (AttributeName) attributeName;
        if(subAttr != null)
            this.subAttr = (SubAttr) subAttr;
    }

    public String getValue() {
        return subAttr == null ? attributeName.getValue() : attributeName.getValue() + subAttr.getValue();
    }

    @Override
    public String toString() {
        return "AttributePath{" +
                "attributeName=" + attributeName +
                ", subAttr=" + subAttr +
                '}';
    }
}

enum CompareOperator {
    eq, ne, co, sw, ew, gt, lt, ge, le;
}

interface Element {
    String getValue();
}

