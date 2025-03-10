/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.sound.midi.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;

/**
 * A {@code MidiFileReader} supplies MIDI file-reading services. Classes
 * implementing this interface can parse the format information from one or more
 * types of MIDI file, and can produce a {@link Sequence} object from files of
 * these types.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class MidiFileReader {

    /**
     * Constructor for subclasses to call.
     */
    protected MidiFileReader() {
    }

    /**
     * Obtains the MIDI file format of the input stream provided. The stream
     * must point to valid MIDI file data. In general, MIDI file readers may
     * need to read some data from the stream before determining whether they
     * support it. These parsers must be able to mark the stream, read enough
     * data to determine whether they support the stream, and, if not, reset the
     * stream's read pointer to its original position. If the input stream does
     * not support this, this method may fail with an {@code IOException}.
     *
     * @param stream the input stream from which file format information should
     *               be extracted
     * @return a {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the stream does not point to valid
     *                                  MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code stream} is {@code null}
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public abstract MidiFileFormat getMidiFileFormat(InputStream stream)
            throws InvalidMidiDataException, IOException;

    /**
     * Obtains the MIDI file format of the {@code URL} provided. The {@code URL}
     * must point to valid MIDI file data.
     *
     * @param url the {@code URL} from which file format information should be
     *            extracted
     * @return a {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the {@code URL} does not point to
     *                                  valid MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code url} is {@code null}
     */
    public abstract MidiFileFormat getMidiFileFormat(URL url)
            throws InvalidMidiDataException, IOException;

    /**
     * Obtains the MIDI file format of the {@code File} provided. The
     * {@code File} must point to valid MIDI file data.
     *
     * @param file the {@code File} from which file format information should
     *             be extracted
     * @return a {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the {@code File} does not point to
     *                                  valid MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code file} is {@code null}
     */
    public abstract MidiFileFormat getMidiFileFormat(File file)
            throws InvalidMidiDataException, IOException;

    /**
     * Obtains a MIDI sequence from the input stream provided. The stream must
     * point to valid MIDI file data. In general, MIDI file readers may need to
     * read some data from the stream before determining whether they support
     * it. These parsers must be able to mark the stream, read enough data to
     * determine whether they support the stream, and, if not, reset the
     * stream's read pointer to its original position. If the input stream does
     * not support this, this method may fail with an {@code IOException}.
     *
     * @param stream the input stream from which the {@code Sequence} should be
     *               constructed
     * @return a {@code Sequence} object based on the MIDI file data contained
     * in the input stream
     * @throws InvalidMidiDataException if the stream does not point to valid
     *                                  MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code stream} is {@code null}
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public abstract Sequence getSequence(InputStream stream)
            throws InvalidMidiDataException, IOException;

    /**
     * Obtains a MIDI sequence from the {@code URL} provided. The {@code URL}
     * must point to valid MIDI file data.
     *
     * @param url the {@code URL} for which the {@code Sequence} should be
     *            constructed
     * @return a {@code Sequence} object based on the MIDI file data pointed to
     * by the {@code URL}
     * @throws InvalidMidiDataException if the {@code URL} does not point to
     *                                  valid MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code url} is {@code null}
     */
    public abstract Sequence getSequence(URL url)
            throws InvalidMidiDataException, IOException;

    /**
     * Obtains a MIDI sequence from the {@code File} provided. The {@code File}
     * must point to valid MIDI file data.
     *
     * @param file the {@code File} from which the {@code Sequence} should be
     *             constructed
     * @return a {@code Sequence} object based on the MIDI file data pointed to
     * by the {@code File}
     * @throws InvalidMidiDataException if the {@code File} does not point to
     *                                  valid MIDI file data recognized by the system
     * @throws IOException              if an I/O exception occurs
     * @throws NullPointerException     if {@code file} is {@code null}
     */
    public abstract Sequence getSequence(File file)
            throws InvalidMidiDataException, IOException;
}
