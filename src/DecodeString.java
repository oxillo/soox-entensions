package soox.saxon.extensions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;
import java.util.Base64;



public class DecodeString extends ExtensionFunctionDefinition {
        @Override
        public StructuredQName getFunctionQName() {
            return new StructuredQName("soox", "simple-open-office-xml", "decode-string");
        }

        @Override
        public int getMinimumNumberOfArguments() {
            return 1;
        }
        
        @Override
        public int getMaximumNumberOfArguments() {
            return 1;
        }
        
        @Override
        public boolean dependsOnFocus() {
            return true; /* to force Saxon call */
        }


        @Override
        public SequenceType[] getArgumentTypes() {
            return new SequenceType[] {SequenceType.makeSequenceType(BuiltInAtomicType.BASE64_BINARY,1)};
        }

        @Override
        public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
            return SequenceType.SINGLE_STRING;
        }

        @Override
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                    Base64BinaryValue value = (Base64BinaryValue)arguments[0].head();
                    byte[] data = value.getBinaryValue();
                    String s = new String( data, java.nio.charset.Charset.forName("UTF-8") );
                    return new StringValue(s);
                }
            };
        }
    }