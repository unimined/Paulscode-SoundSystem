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

public class Instrument {
	public String name;
	public int    vibrato_type, vibrato_sweep;
	public int vibrato_depth, vibrato_rate;
	public boolean volume_envelope_active, panning_envelope_active;
	public int volume_fade_out;

	private Envelope volume_envelope, panning_envelope;
	private final int[]    key_to_sample;
	private       Sample[] samples;

	public Instrument() {
		name = "";
		set_volume_envelope(new Envelope());
		set_panning_envelope(new Envelope());
		key_to_sample = new int[96];
		set_num_samples(1);
	}

	public Envelope get_volume_envelope() {
		return volume_envelope;
	}

	public void set_volume_envelope(Envelope envelope) {
		if (envelope != null) {
			volume_envelope = envelope;
		}
	}

	public Envelope get_panning_envelope() {
		return panning_envelope;
	}

	public void set_panning_envelope(Envelope envelope) {
		if (envelope != null) {
			panning_envelope = envelope;
		}
	}

	public Sample get_sample_from_key(int key) {
		int sample_idx;
		sample_idx = 0;
		if (key > 0 && key <= key_to_sample.length) {
			sample_idx = key_to_sample[key - 1];
		}
		return get_sample(sample_idx);
	}

	public void set_key_to_sample(int key, int sample) {
		if (key > 0 && key <= key_to_sample.length) {
			key_to_sample[key - 1] = sample;
		}
	}

	public int get_num_samples() {
		return samples.length;
	}

	public void set_num_samples(int num_samples) {
		if (num_samples < 1) {
			num_samples = 1;
		}
		samples = new Sample[num_samples];
		set_sample(0, null);
	}

	public Sample get_sample(int sample_index) {
		Sample sample;
		sample = null;
		if (sample_index >= 0 && sample_index < samples.length) {
			sample = samples[sample_index];
		}
		if (sample == null) {
			sample = samples[0];
		}
		return sample;
	}

	public void set_sample(int sample_index, Sample sample) {
		if (sample_index >= 0 && sample_index < samples.length) {
			samples[sample_index] = sample;
		}
		if (samples[0] == null) {
			samples[0] = new Sample();
		}
	}
}
