/*
 * IBXM is copyright (c) 2007, Martin Cameron, and is licensed under the BSD License.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of mumart nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ibxm;

@SuppressWarnings("unused")
public class Module {
	public String  song_title;
	public boolean linear_periods, fast_volume_slides, pal;
	public int global_volume, channel_gain;
	public int default_speed, default_tempo;
	public int restart_sequence_index;

	private int[] initial_panning, sequence;
	private Pattern[]    patterns;
	private Instrument[] instruments;

	private final Pattern    default_pattern;
	private final Instrument default_instrument;

	public Module() {
		song_title = IBXM.VERSION;
		set_num_channels(1);
		set_sequence_length(1);
		set_num_patterns(0);
		set_num_instruments(0);
		default_pattern = new Pattern();
		default_instrument = new Instrument();
	}

	public int get_num_channels() {
		return initial_panning.length;
	}

	public void set_num_channels(int num_channels) {
		if (num_channels < 1) {
			num_channels = 1;
		}
		initial_panning = new int[num_channels];
	}

	public int get_initial_panning(int channel) {
		int panning;
		panning = 128;
		if (channel >= 0 && channel < initial_panning.length) {
			panning = initial_panning[channel];
		}
		return panning;
	}

	public void set_initial_panning(int channel, int panning) {
		if (channel >= 0 && channel < initial_panning.length) {
			initial_panning[channel] = panning;
		}
	}

	public int get_sequence_length() {
		return sequence.length;
	}

	public void set_sequence_length(int sequence_length) {
		if (sequence_length < 0) {
			sequence_length = 0;
		}
		sequence = new int[sequence_length];
	}

	public void set_sequence(int sequence_index, int pattern_index) {
		if (sequence_index >= 0 && sequence_index < sequence.length) {
			sequence[sequence_index] = pattern_index;
		}
	}

	public int get_num_patterns() {
		return patterns.length;
	}

	public void set_num_patterns(int num_patterns) {
		if (num_patterns < 0) {
			num_patterns = 0;
		}
		patterns = new Pattern[num_patterns];
	}

	public Pattern get_pattern_from_sequence(int sequence_index) {
		Pattern pattern;
		pattern = default_pattern;
		if (sequence_index >= 0 && sequence_index < sequence.length) {
			pattern = get_pattern(sequence[sequence_index]);
		}
		return pattern;
	}

	public Pattern get_pattern(int pattern_index) {
		Pattern pattern;
		pattern = null;
		if (pattern_index >= 0 && pattern_index < patterns.length) {
			pattern = patterns[pattern_index];
		}
		if (pattern == null) {
			pattern = default_pattern;
		}
		return pattern;
	}

	public void set_pattern(int pattern_index, Pattern pattern) {
		if (pattern_index >= 0 && pattern_index < patterns.length) {
			patterns[pattern_index] = pattern;
		}
	}

	public int get_num_instruments() {
		return instruments.length;
	}

	public void set_num_instruments(int num_instruments) {
		if (num_instruments < 0) {
			num_instruments = 0;
		}
		instruments = new Instrument[num_instruments];
	}

	public Instrument get_instrument(int instrument_index) {
		Instrument instrument;
		instrument = null;
		if (instrument_index > 0 && instrument_index <= instruments.length) {
			instrument = instruments[instrument_index - 1];
		}
		if (instrument == null) {
			instrument = default_instrument;
		}
		return instrument;
	}

	public void set_instrument(int instrument_index, Instrument instrument) {
		if (instrument_index > 0 && instrument_index <= instruments.length) {
			instruments[instrument_index - 1] = instrument;
		}
	}
}

