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

public class Pattern {
	public int num_rows;

	private int data_offset, note_index;
	private byte[] pattern_data;

	public Pattern() {
		num_rows = 1;
		set_pattern_data(new byte[0]);
	}

	public void set_pattern_data(byte[] data) {
		if (data != null) {
			pattern_data = data;
		}
		data_offset = 0;
		note_index = 0;
	}

	public void get_note(int[] note, int index) {
		if (index < note_index) {
			note_index = 0;
			data_offset = 0;
		}
		while (note_index <= index) {
			data_offset = next_note(data_offset, note);
			note_index += 1;
		}
	}

	public int next_note(int data_offset, int[] note) {
		int bitmask, field;
		if (data_offset < 0) {
			data_offset = pattern_data.length;
		}
		bitmask = 0x80;
		if (data_offset < pattern_data.length) {
			bitmask = pattern_data[data_offset] & 0xFF;
		}
		if ((bitmask & 0x80) == 0x80) {
			data_offset += 1;
		} else {
			bitmask = 0x1F;
		}
		for (field = 0; field < 5; field++) {
			note[field] = 0;
			if ((bitmask & 0x01) == 0x01) {
				if (data_offset < pattern_data.length) {
					note[field] = pattern_data[data_offset] & 0xFF;
					data_offset += 1;
				}
			}
			bitmask = bitmask >> 1;
		}
		return data_offset;
	}
}

