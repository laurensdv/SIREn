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
 * @project siren-benchmark
 * @author Renaud Delbru [ 9 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 * @author Campinas Stephane [ 3 Jun 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.benchmark.generator.document;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.sindice.siren.benchmark.util.RdfJsonUtil;
import org.sindice.siren.benchmark.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the Document-Entity archive format of the Sindice-2011 dataset.
 * An entity consists in the triples in docIdxx/entityIDyy/*.nt
 */
public class SindiceDocumentProvider extends DocumentProvider {

  private final SindiceEntityReader reader;

  /**
   * the current document
   */
  private final SindiceDocument document = new SindiceDocument();

  /**
   * the current entity
   */
  private SindiceEntity entity = null;

  protected final Logger logger = LoggerFactory.getLogger(SindiceDocumentProvider.class);

  public SindiceDocumentProvider(final File input) throws IOException {
    reader = new SindiceEntityReader(input);
  }

  public boolean hasNext() {
    return (entity != null) || reader.hasNext();
  }

  @Override
  public BenchmarkDocument next() {
    document.clear();
    String context = new String();

    try {
      if (entity != null) { // process previous entity
        context = entity.getContext();
        document.put(entity.getSubject(), entity.getJsonNode());
      }

      while (reader.hasNext()) {
        // fetch next entity
        entity = reader.next();

        // if first entity, set context
        if (context.isEmpty()) {
          context = entity.getContext();
        }

        // if context has changed, return document
        if (!context.equals(entity.getContext())) {
          document.putUrl(context);
          document.generateContentField();
          return document;
        }

        // add entity to the document
        document.put(entity.getSubject(), entity.getJsonNode());
      }

      // no more entity, return last document
      entity = null;
      document.putUrl(context);
      document.generateContentField();
      return document;
    }
    catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    try {
      if (reader != null) {
        reader.close();
      }
    }
    catch (final IOException e) {
      logger.error("Error when closing entity reader", e);
    }
  }

  private class SindiceEntityReader implements Iterator<SindiceEntity>, Closeable {

    /* The dataset files */
    protected final File[] input;
    protected int inputPos = 0;

    /**
     * The current reader into the compressed archive
     */
    protected TarArchiveInputStream reader = null;

    /**
     * A file entry in the archive
     */
    protected TarArchiveEntry tarEntry;

    /**
     * Byte array used for reading the compressed tar files
     */
    private final ByteBuffer bbuffer = ByteBuffer.allocate(16384);

    /**
     * the current entity
     */
    private final SindiceEntity entity = new SindiceEntity();

    public SindiceEntityReader(final File input) throws IOException {
      this.input = input.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
          return name.matches(SindiceEntityReader.this.getPattern());
        }

      });

      reader = this.getTarInputStream(this.input[0]);
      logger.info("Creating index from input located at {} ({} files)",
        input.getAbsolutePath(), this.input.length);
      logger.info("Reading dump: {}", this.input[0]);
    }

    /**
     * The regular expression of the input files
     */
    protected String getPattern() {
      return "DE-[0-9]+\\.tar\\.gz";
    }

    /**
     * Create a buffered tar input stream from the file in
     * @param in
     * @throws FileNotFoundException
     * @throws IOException
     */
    private TarArchiveInputStream getTarInputStream(final File in)
    throws FileNotFoundException, IOException {
      return new TarArchiveInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(in))));
    }

    /**
     * Read the tar entry
     */
    public void readFile(final String tarEntryName) throws IOException {
      if (tarEntryName.endsWith("metadata")) {
        this.readFile(tarEntry.getSize(), entity.sbMetadata);
      }
      else if (tarEntryName.endsWith("outgoing-triples.nt")) {
        this.readFile(tarEntry.getSize(), entity.sbNTriples);
      }
      else if (tarEntryName.endsWith("incoming-triples.nt")) {
        reader.skip(reader.available()); // skip entry
      }
      else {
        throw new IOException("Unknown entry file: " + tarEntryName);
      }
    }

    /**
     * Read size bytes from the reader at the current position
     *
     * @param size
     * the number of bytes to read
     * @param data
     * the buffer to store the content
     * @throws IOException
     */
    public void readFile(long size, final StringBuilder data)
    throws IOException {
      bbuffer.clear();
      while (size > bbuffer.capacity()) {
        reader.read(bbuffer.array(), 0, bbuffer.capacity());
        size -= bbuffer.capacity();
        this.toAsciiString(data, bbuffer.capacity());
        bbuffer.clear();
      }
      reader.read(bbuffer.array(), 0, (int) size);
      this.toAsciiString(data, (int) size);
    }

    /**
     * Convert the byte array in the platform encoding
     * @param data the string buffer
     * @param length number of bytes to decode
     */
    private final void toAsciiString(final StringBuilder data, final int length) {
      for (int i = 0; i < length; i++) {
        data.append(StringUtil.BYTE_TO_CHARS[bbuffer.get(i) - Byte.MIN_VALUE]);
      }
    }

    @Override
    public boolean hasNext() {
      try {
        do {
          /*
           * if reader.available() is not equal to 0, then it means that this entry
           * has been loaded, but not read.
           */
          while (reader.available() == 0 && (tarEntry = reader.getNextTarEntry()) == null) { // Next tar entry
            if (++inputPos >= input.length) {
              reader.close();
              return false;
            }
            // Next archive file
            reader.close();
            logger.info("Reading dump: {}", this.input[inputPos]);
            reader = this.getTarInputStream(input[inputPos]);
          }
        } while (tarEntry != null && tarEntry.isDirectory()); // skip directories
      }
      catch (final IOException e) {
        logger.error("Error while reading the input: {}\n{}", input[inputPos], e);
      }
      return true;
    }

    @Override
    public SindiceEntity next() {
      entity.clear();
      try {
        for (int i = 0; i < 3; i++) {
          if (!this.hasNext()) {
            logger.info("Error while trying to get entry file from {}",
              input[inputPos].getAbsolutePath());
            throw new NoSuchElementException("entry file missing");
          }
          this.readFile(tarEntry.getName());
        }
      }
      catch (final IOException e) {
        logger.info("Error while trying to read archive {}",
          input[inputPos].getAbsolutePath());
      }
      return entity;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      if (reader != null) {
        reader.close();
      }
    }

  }

  /**
   * A benchmark document from the sindice 2011 dataset
   */
  private class SindiceDocument extends BenchmarkDocument {

    final ObjectMapper mapper;
    final ObjectNode rootNode;

    public SindiceDocument() {
      mapper = new ObjectMapper();
      rootNode = mapper.createObjectNode();
    }

    public void put(final String entityId, final JsonNode entity) {
      rootNode.put(entityId, entity.get(entityId));
    }

    public void generateContentField() throws IOException {
      // convert root node into json and update content field
      this.putContent(mapper.writeValueAsString(rootNode));
      // clean json objects
      rootNode.removeAll();
    }

    @Override
    public void clear() {
      super.clear();
      rootNode.removeAll();
    }

  }

  /**
   * An entity from the Sindice 2011 dataset
   */
  private class SindiceEntity {

    /* metadata */
    final StringBuilder sbMetadata = new StringBuilder(256);

    /* NTriples */
    final StringBuilder sbNTriples = new StringBuilder(4096);

    public void clear() {
      sbNTriples.setLength(0);
      sbMetadata.setLength(0);
    }

    public String getSubject() {
      final int newLine = this.sbMetadata.indexOf("\n");
      return this.sbMetadata.substring(newLine + 1).trim();
    }

    public String getContext() {
      final int newLine = this.sbMetadata.indexOf("\n");
      return this.sbMetadata.substring(0, newLine).trim();
    }

    public JsonNode getJsonNode() throws IOException {
      try {
        return RdfJsonUtil.convertNTriplesEntity(this.getSubject(), this.sbNTriples.toString());
      }
      catch (final RDFParseException e) {
        logger.error("Cannot recover RDF parse error. Skipped entity.", e);
        return null;
      }
      catch (final RDFHandlerException e) {
        logger.error("Unrecoverable RDF handler error. Skipped entity.", e);
        return null;
      }
    }

  }

  public static void main(final String[] args) throws IOException {
    final File input = new File("./src/test/resources/dataset/sindice/");
    final SindiceDocumentProvider reader = new SindiceDocumentProvider(input);
    BenchmarkDocument doc;
    while (reader.hasNext()) {
      doc = reader.next();
      System.out.println(doc.getContent());
    }
  }

}
