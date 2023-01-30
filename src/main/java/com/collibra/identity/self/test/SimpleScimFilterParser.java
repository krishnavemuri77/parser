package com.collibra.identity.self.test;

import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.Scanner;
import java.util.function.Function;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

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
@BuildParseTree
public class SimpleScimFilterParser extends BaseParser<Object> {

//    Rule filter() {
//        return attrExp();
//    }

    Rule nameChar() {
        return FirstOf(
                Ch('-'),
                Ch('_'),
                CharRange('0', '9'),
                CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    public Rule alpha() {
        return Sequence(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')), push(matchedChar()));
    }

    // works for positive cases and fails for negative
    // to Error on -ve case input a* I've added TestNot(). should find a work around or extend the range of possibilities.
    Rule attrName() {
        return Sequence(alpha(), Sequence(ZeroOrMore(nameChar()), push(matchOrDefault("")), TestNot("*"), push(String.valueOf(pop(1)) + pop())));
    }

    // * subAttr   = "." ATTRNAME
    Rule subAttr() {
        return Sequence(Optional(".", attrName()), push(match().isEmpty() ? "" : "." + pop()), TestNot("."));
    }

    // * attrPath  = attrName *1subAttr
    Rule attrPath() {
        return Sequence(attrName(), subAttr(), push(String.valueOf(pop(1)) + pop()));
    }

    String getNameChars() {
        return "-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYX0123456789";
    }


    //  * ATTRNAME  = ALPHA *(nameChar)
//    Rule attrName() {
//        Var<String> nameChar = new Var();
//        return Sequence(alpha(), ZeroOrMore(nameChar()), push(match()), new Action<>() {
//            @Override
//            public boolean run(Context<Object> context) {
//                String matchedInout = match();
//                return true;
//            }
//        });
//        return Sequence(alpha(), push(match()), Sequence(ZeroOrMore(nameChar()), nameChar.set(matchOrDefault(""))), new Action<>() {
//            @Override
//            public boolean run(Context<Object> context) {
//                if(!nameChar.get().isEmpty()) {
//                    context.getValueStack().push(new AttributeName(pop(), nameChar.get()));
//                } else {
//                    context.getValueStack().push(new AttributeName(pop()));
//                }
//                return true;
//            }
//        });
//    }

    Rule compareOp() {
        return FirstOf("eq", "ne", "co", "sw", "ew", "gt", "lt", "ge", "le");
    }

    Rule compValue() {
        return FirstOf(
                "false",
                "null",
                "true",
                number()
        );
    }


    //  * attrPath  = attrName *1subAttr
//    Rule attrPath() {
//        Var<SubAttr> subAttribute = new Var();
//        return Sequence(attrName(), Optional(subAttr(subAttribute)), new Action<>() {
//            @Override
//            public boolean run(Context<Object> context) {
//                if(subAttribute.get() != null) {
//                    SubAttr subAttr = (SubAttr) subAttribute.get();
//                    AttributeName attributeName = (AttributeName) pop();
//                    if(subAttr.getValue().isEmpty()) {
//                        context.getValueStack().push(new AttributePath(attributeName));
//                    } else {
//                        context.getValueStack().push(new AttributePath(attributeName, subAttr));
//                    }
//                } else {
//                    AttributeName attributeName = (AttributeName) pop();
//                    context.getValueStack().push(new AttributePath(attributeName));
//                }
//                return true;
//            };
//        });
//    }

    // attrExp   = (attrPath "pr") / (attrPath compareOp compValue)
//    Rule attrExp() {
//        return Sequence("(", attrPath(), space(), "pr", ")", push(new AttributePresentExpression(pop())));
//                //Sequence("(", attrPath(), space(), compareOp(), space(), compValue(), ")", push(new AttributeEqualsExpression("Attribute equals Expression", "attribute")))
//    }

    Rule number() {
        return OneOrMore(CharRange('0', '9'));
    }

    public Rule space() {
        return ZeroOrMore(" ");
    }

    public Rule digit() {
        return Sequence(CharRange('0', '9'), push(match()));
    }


    public Rule alphabets() {
        return OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')));
    }

    public static void main(String[] args) {

        SimpleScimFilterParser parser = Parboiled.createParser(SimpleScimFilterParser.class);

        while (true) {
            System.out.print("Enter a filter expression (single RETURN to exit)!\n");

            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<?> result = new ReportingParseRunner(parser.attrPath()).run(input);

            if (!result.parseErrors.isEmpty())
                System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
            else {
                //System.out.println(((AttributePresentExpression)result.resultValue).getValue());
                System.out.println(result.resultValue);
                printNodeTree(result);
            }
        }
    }

}

class AttributePresentExpression {
    String value;
    private AttributePath attributePath;

    AttributePresentExpression(Object path) {
        this.attributePath = (AttributePath) path;
    }

    public String getValue() {
        return attributePath.getValue() + " pr ";
    }

    @Override
    public String toString() {
        return "AttributePresentExpression{" +
                "value='" + " pr " + '\'' +
                ", attributePath=" + attributePath +
                '}';
    }
}

class AttributeEqualsExpression {
    String value;
    private AttributePath attributePath;
    private Enum compareOperator;
    private String compareValue;

    AttributeEqualsExpression(AttributePath path, CompareOperator compareOperator, String value) {
        this.attributePath = path;
        this.compareOperator = compareOperator;
        this.compareValue = value;
    }

    public String getValue() {
        return attributePath + " pr ";
    }

    @Override
    public String toString() {
        return "AttributePresentExpression{" +
                "attributePath=" + attributePath +
                '}';
    }
}

class AttributeName {

    String value;
    private String alpha;
    private String nameChar;

    AttributeName(Object alpha) {
        this.alpha = (String) alpha;
    }

    AttributeName(Object alpha, String nameChar) {
        this(alpha);
        this.nameChar = nameChar;
    }

    public String getValue() {
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

class NameChar {

    String value;

    NameChar(Object value) {
        this.value = (String) value;
    }

    @Override
    public String toString() {
        return "NameChar{" +
                "value='" + value + '\'' +
                '}';
    }
}

class SubAttr {

    String value;
    AttributeName attributeName;

    SubAttr(Object attributeName) {
        this.attributeName = (AttributeName) attributeName;
    }

    public String getValue() {
        return "." + attributeName.getValue();
    }

    @Override
    public String toString() {
        return "SubAttr{" +
                "attributeName=" + attributeName +
                '}';
    }
}

class AttributePath {

    String value;
    private AttributeName attributeName;
    private SubAttr subAttr;

    public AttributePath(AttributeName attributeName) {
        this.attributeName = attributeName;
    }

    public AttributePath(Object attributeName, Object subAttr) {
        this((AttributeName) attributeName);
        this.subAttr = (SubAttr) subAttr;
    }

    public String getValue() {
        if (subAttr != null)
            return attributeName.getValue() + subAttr.getValue();
        else
            return attributeName.getValue();
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

