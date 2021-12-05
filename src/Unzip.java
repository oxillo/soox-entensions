/**********************************************************/
/* Copyright (C) 2021 Olivier XILLO                       */
/* Licensed under MIT                                     */
/**********************************************************/

package soox.saxon.extensions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.tree.iter.AtomicIterator;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;



public class Unzip extends ExtensionFunctionDefinition {
        @Override
        public StructuredQName getFunctionQName() {
            return new StructuredQName("soox", "simplify-office-open-xml", "unzip");
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
            return new SequenceType[] {SequenceType.SINGLE_STRING};
        }

        @Override
        public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
            MapType mt = new MapType( BuiltInAtomicType.STRING, MapType.SINGLE_MAP_ITEM); 
            return SequenceType.makeSequenceType(mt,StaticProperty.EXACTLY_ONE);
        }

        @Override
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                    // The zip file name is the first (and only) argument
                    String zipfilename = arguments[0].head().getStringValue();

                    // we'll store unzipped file content in a map
                    HashTrieMap filesMap = new HashTrieMap();
                    try {
                        // open the file as zip
                        //File zipfile = new File(zipfilename);
                        FileInputStream fis = new FileInputStream(zipfilename);
                        ZipInputStream zis = new ZipInputStream(fis);
                        
                        ZipEntry zen;
                        while( (zen = zis.getNextEntry()) != null){
                            // Retrieve file name
                            String filename = zen.getName();

                            // Retrieve file content
                            ByteArrayOutputStream content = new ByteArrayOutputStream(); 
                            byte[] buffer = new byte[1024]; 
                            int len; 
                            while ( (len = zis.read(buffer)) > 0 ) {content.write(buffer, 0, len);}
                            String s = new String( content.toByteArray(), java.nio.charset.Charset.forName("UTF-8") );
                            //HashTrieMap file = new HashTrieMap();
                            //file = file.addEntry( new StringValue("content"), new StringValue(s) );
                            filesMap = filesMap.addEntry( new StringValue(filename), 
                                HashTrieMap.singleton( new StringValue("content"), new StringValue(s) )
                            );

                            zis.closeEntry();
                        }
                        zis.close();
                        fis.close();
                    } catch ( IOException e ) {
                        // TODO :process exception to return XPathException
                        e.printStackTrace();
                    }
                    return filesMap;
                }
            };
        }
    }