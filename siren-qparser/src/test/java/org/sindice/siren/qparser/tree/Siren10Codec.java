package org.sindice.siren.qparser.tree;

import java.util.HashSet;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene40.Lucene40Codec;
import org.apache.lucene.codecs.lucene40.Lucene40PostingsFormat;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsFormat;

public class Siren10Codec extends Lucene40Codec {

  final PostingsFormat lucene40 = new Lucene40PostingsFormat();
  PostingsFormat defaultTestFormat;
  final HashSet<String> sirenFields = new HashSet<String>();

  public Siren10Codec() {
    this(256);
  }

  public Siren10Codec(final int blockSize) {
    this.defaultTestFormat = new Siren10PostingsFormat(blockSize);
    Codec.setDefault(this);
  }

  public void resetSirenFields() {
    sirenFields.clear();
  }

  public void addSirenField(String field) {
    sirenFields.add(field);
  }

  @Override
  public PostingsFormat getPostingsFormatForField(final String field) {
    if (sirenFields.contains(field)) {
      return defaultTestFormat;
    }
    else {
      return lucene40;
    }
  }

  @Override
  public String toString() {
    return "Siren10Codec[" + defaultTestFormat.toString() + "]";
  }

}