/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.lib.parser;

import com.streamsets.pipeline.lib.data.DataFactory;
import com.streamsets.pipeline.lib.io.OverrunReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class CharDataParserFactory extends DataFactory {

  protected CharDataParserFactory(Settings settings) {
    super(settings);
  }

  public DataParser getParser(String id, String data) throws DataParserException {
    return getParser(id, new ByteArrayInputStream(data.getBytes(getSettings().getCharset())), 0);
  }

  public DataParser getParser(File file, long fileOffset)
    throws DataParserException {
    try {
      return getParser(file.getName(), new FileInputStream(file), fileOffset);
    } catch (FileNotFoundException e) {
      throw new DataParserException(Errors.DATA_PARSER_00, file.getAbsolutePath(), e.getMessage(), e);
    }
  }

  public abstract DataParser getParser(String id, InputStream is, long offset) throws DataParserException;

  protected OverrunReader createReader(InputStream is) {
    Reader bufferedReader = new BufferedReader(new InputStreamReader(is, getSettings().getCharset()));
    OverrunReader overrunReader = new OverrunReader(bufferedReader, getSettings().getOverRunLimit(), false);
    return overrunReader;
  }

}
