/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren-core
 * @author Renaud Delbru [ 19 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.block;

import java.io.IOException;

import org.junit.Test;
import org.sindice.siren.index.codecs.CodecTestCase;
import org.sindice.siren.index.codecs.siren10.DocsFreqBlockIndexInput;
import org.sindice.siren.index.codecs.siren10.DocsFreqBlockIndexInput.DocsFreqBlockReader;
import org.sindice.siren.index.codecs.siren10.DocsFreqBlockIndexOutput;
import org.sindice.siren.index.codecs.siren10.DocsFreqBlockIndexOutput.DocsFreqBlockWriter;
import org.sindice.siren.index.codecs.siren10.Siren10BlockStreamFactory;

public class TestVariableIntCodec extends CodecTestCase {

  private DocsFreqBlockIndexOutput getIndexOutput(final int blockSize) throws IOException {
    final Siren10BlockStreamFactory factory = new Siren10BlockStreamFactory(blockSize);
    factory.setDocsBlockCompressor(new VIntBlockCompressor());
    factory.setFreqBlockCompressor(new VIntBlockCompressor());
    return factory.createDocsFreqOutput(directory, "test", newIOContext(random()));
  }

  private DocsFreqBlockIndexInput getIndexInput() throws IOException {
    final Siren10BlockStreamFactory factory = new Siren10BlockStreamFactory(0);
    factory.setDocsBlockDecompressor(new VIntBlockDecompressor());
    factory.setFreqBlockDecompressor(new VIntBlockDecompressor());
    return factory.openDocsFreqInput(directory, "test", newIOContext(random()));
  }

  public void testReadDoc() throws IOException {

    final DocsFreqBlockIndexOutput out = this.getIndexOutput(512);
    final DocsFreqBlockWriter writer = out.getBlockWriter();

    writer.setNodeBlockIndex(out.index());
    writer.setPosBlockIndex(out.index());
    for (int i = 0; i < 11777; i++) {
      if (writer.isFull()) {
        writer.flush();
      }
      writer.write(i);
    }

    writer.flush(); // flush remaining data
    out.close();

    final DocsFreqBlockIndexInput in = this.getIndexInput();
    final DocsFreqBlockReader reader = in.getBlockReader();

    reader.setNodeBlockIndex(in.index());
    reader.setPosBlockIndex(in.index());
    for (int i = 0; i < 11777; i++) {
      if (reader.isExhausted()) {
        reader.nextBlock();
      }
      assertEquals(i, reader.nextDocument());
    }

    in.close();
  }

  public void testReadDocAndFreq() throws IOException {

    final DocsFreqBlockIndexOutput out = this.getIndexOutput(512);
    final DocsFreqBlockWriter writer = out.getBlockWriter();

    writer.setNodeBlockIndex(out.index());
    writer.setPosBlockIndex(out.index());
    for (int i = 0; i < 11777; i++) {
      if (writer.isFull()) {
        writer.flush();
      }
      writer.write(i);
      writer.writeNodeFreq(random().nextInt(10) + 1);
    }

    writer.flush(); // flush remaining data
    out.close();

    final DocsFreqBlockIndexInput in = this.getIndexInput();
    final DocsFreqBlockReader reader = in.getBlockReader();

    reader.setNodeBlockIndex(in.index());
    reader.setPosBlockIndex(in.index());
    for (int i = 0; i < 11777; i++) {
      if (reader.isExhausted()) {
        reader.nextBlock();
      }
      assertEquals(i, reader.nextDocument());
      final int frq = reader.nextNodeFreq();
      assertTrue(frq > 0);
      assertTrue(frq <= 10);
    }

    in.close();
  }

  @Override
  public void doTest(final int[] values, final int blockSize) throws Exception {
    this.tearDown();
    this.setUp();

    final DocsFreqBlockIndexOutput out = this.getIndexOutput(blockSize);
    final DocsFreqBlockWriter writer = out.getBlockWriter();

    writer.setNodeBlockIndex(out.index());
    writer.setPosBlockIndex(out.index());
    for (final int value : values) {
      if (writer.isFull()) {
        writer.flush();
      }
      writer.write(value);
      writer.writeNodeFreq(value);
    }

    writer.flush(); // flush remaining data
    out.close();

    final DocsFreqBlockIndexInput in = this.getIndexInput();
    final DocsFreqBlockReader reader = in.getBlockReader();

    reader.setNodeBlockIndex(in.index());
    reader.setPosBlockIndex(in.index());
    for (final int value : values) {
      if (reader.isExhausted()) {
        reader.nextBlock();
      }
      assertEquals(value, reader.nextDocument());
      assertEquals(value, reader.nextNodeFreq());
    }

    in.close();
  }

  @Test
  public void testIntegerRange() throws Exception {
    this.doTestIntegerRange(1, 32);
  }

//  @Test
//  public void testIncompleteBlock() throws IOException {
//    final RAMDirectory dir = new RAMDirectory();
//    final String filename = BasicPFor.class.toString();
//    final VariableIntegerBlockIndexOutput output = new VariableIntegerBlockIndexOutput(dir, filename, 32, new BasicVariableInteger());
//
//    final int[] values = {0,1,0,0,1,4,2,4,1,1,2,16,0,1,1,0};
//    for (final int element : values) {
//      output.write(element);
//    }
//    output.close();
//
//    final VariableIntegerBlockIndexInput input = new VariableIntegerBlockIndexInput(dir, filename, 32, new BasicVariableInteger());
//    final Reader reader = input.reader();
//    for (int i = 0; i < values.length; i++) {
//      assertEquals("Error at record " + i, values[i], reader.next());
//    }
//    input.close();
//    dir.close();
//
//  }

}
