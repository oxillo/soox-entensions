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
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.tree.iter.AtomicIterator;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;



public class Zip extends ExtensionFunctionDefinition {
        @Override
        public StructuredQName getFunctionQName() {
            return new StructuredQName("soox", "simplify-office-open-xml", "zip");
        }

        @Override
        public int getMinimumNumberOfArguments() {
            return 1;
        }
        
        @Override
        public int getMaximumNumberOfArguments() {
            return 2;
        }
        
        @Override
        public boolean dependsOnFocus() {
            return true; /* to force Saxon call */
        }


        @Override
        public SequenceType[] getArgumentTypes() {
            //MapType mt = new MapType(BuiltInAtomicType.STRING,SequenceType.makeSequenceType(BuiltInAtomicType.BASE64_BINARY, 1));
            //map(xs:string, map(xs:string,item(*))
            MapType mt1 = new MapType( BuiltInAtomicType.STRING, MapType.SINGLE_MAP_ITEM); 
            MapType mt = new MapType( BuiltInAtomicType.STRING, SequenceType.makeSequenceType(mt1, StaticProperty.EXACTLY_ONE));
            return new SequenceType[] {SequenceType.makeSequenceType(mt1,StaticProperty.EXACTLY_ONE),
                MapType.OPTIONAL_MAP_ITEM};
            //return new MapType(BuiltInAtomicType.STRING, SequenceType.makeSequenceType(valueType, 1));
            //return new SequenceType[] {SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, StaticProperty.EXACTLY_ONE)};
        }

        @Override
        public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
            return SequenceType.EMPTY_SEQUENCE;
        }

        @Override
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                    MapItem files = (MapItem)arguments[0].head();
                    AtomicIterator filenames = files.keys();

                    String zipfilename = null;
                    if( arguments.length>=2) {
                        MapItem options = (MapItem)arguments[1].head();
                        GroundedValue uri = options.get(new StringValue("uri"));
                        if( uri!=null) {
                            zipfilename = new String(uri.getStringValue());
                            //java.lang.System.out.println("Uri : "+((StringValue)uri).getStringValue());
                        }
                    }

                    try{
                        // Create Zip in memory
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(bos);

                        // Iterate over the keys( = filename ) of the map and write content to Zip
                        AtomicValue filename;
                        while( (filename = filenames.next()) != null ){
                            // The relative file name is in the key
                            // Retrieve file encoding and content
                            MapItem file = (MapItem) files.get(filename);
                            /*GroundedValue encoding = file.get( new StringValue("encoding") );
                            if( encoding != null ){
                                java.lang.System.out.println("file " + filename + " has encoding");
                            }else{
                                java.lang.System.out.println("file " + filename + " does not have encoding");
                            }*/
                            GroundedValue content = file.get(new StringValue("content"));
                            if( content != null ){
                                byte[] data = {};
                                if( Type.getItemType( content.head(), null )==BuiltInAtomicType.BASE64_BINARY ){
                                    data = ((Base64BinaryValue)content).getBinaryValue();
                                }
                                if( Type.getItemType( content.head(), null )==BuiltInAtomicType.STRING ){
                                    data = ((StringValue)content).getStringValue().getBytes(java.nio.charset.Charset.forName("UTF-8"));
                                }
                                // Add zip entry when we have a data
                                if( data.length>0 ){
                                    ZipEntry zen = new ZipEntry(filename.getStringValue());
                                    //adding to zipoutputstream
                                    zos.putNextEntry(zen);
                                    zos.write(data, 0, data.length);
                                    zos.closeEntry();
                                }
                            }
                        }
                        zos.close();
                        
                        //create a new zip file in which all input files have to be zipped.
                        if( zipfilename != null ){
                            File zipFile = new File(zipfilename);
                            //create output stream for the zipfile.
                            FileOutputStream fos = new FileOutputStream(zipFile);
                            bos.writeTo(fos);
                            fos.close();
                        }
                        bos.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    return EmptySequence.getInstance();
                }
            };
        }
    }