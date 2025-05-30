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
public class Envelope {
	public boolean sustain, looped;
	private int sustain_tick, loop_start_tick, loop_end_tick;
	private int[] ticks, ampls;

	public Envelope() {
		set_num_points(1);
	}

	public void set_num_points(int num_points) {
		if (num_points <= 0) {
			num_points = 1;
		}
		ticks = new int[num_points];
		ampls = new int[num_points];
		set_point(0, 0, 0, false);
	}

	/* When you set a point, all subsequent points are reset. */
	public void set_point(int point, int tick, int ampl, boolean delta) {
		if (point >= 0 && point < ticks.length) {
			if (point == 0) {
				tick = 0;
			}
			if (point > 0) {
				if (delta) tick += ticks[point - 1];
				if (tick <= ticks[point - 1]) {
					System.out.println("Envelope: Point not valid (" + tick + " <= " + ticks[point - 1] + ")");
					tick = ticks[point - 1] + 1;
				}
			}
			ticks[point] = tick;
			ampls[point] = ampl;
			point += 1;
			while (point < ticks.length) {
				ticks[point] = ticks[point - 1] + 1;
				ampls[point] = 0;
				point += 1;
			}
		}
	}

	public void set_sustain_point(int point) {
		if (point < 0) {
			point = 0;
		}
		if (point >= ticks.length) {
			point = ticks.length - 1;
		}
		sustain_tick = ticks[point];
	}

	public void set_loop_points(int start, int end) {
		if (start < 0) {
			start = 0;
		}
		if (start >= ticks.length) {
			start = ticks.length - 1;
		}
		if (end < start || end >= ticks.length) {
			end = start;
		}
		loop_start_tick = ticks[start];
		loop_end_tick = ticks[end];
	}

	public int next_tick(int tick, boolean key_on) {
		tick = tick + 1;
		if (looped && tick >= loop_end_tick) {
			tick = loop_start_tick;
		}
		if (sustain && key_on && tick >= sustain_tick) {
			tick = sustain_tick;
		}
		return tick;
	}

	public int calculate_ampl(int tick) {
		int idx, point, delta_t, delta_a, ampl;
		ampl = ampls[ticks.length - 1];
		if (tick < ticks[ticks.length - 1]) {
			point = 0;
			for (idx = 1; idx < ticks.length; idx++) {
				if (ticks[idx] <= tick) {
					point = idx;
				}
			}
			delta_t = ticks[point + 1] - ticks[point];
			delta_a = ampls[point + 1] - ampls[point];
			ampl = (delta_a << IBXM.FP_SHIFT) / delta_t;
			ampl = ampl * (tick - ticks[point]) >> IBXM.FP_SHIFT;
			ampl = ampl + ampls[point];
		}
		return ampl;
	}

	public void dump() {
		int idx, tick;
		for (idx = 0; idx < ticks.length; idx++) {
			System.out.println(ticks[idx] + ", " + ampls[idx]);
		}
		for (tick = 0; tick < 222; tick++) {
			System.out.print(calculate_ampl(tick) + ", ");
		}
	}
}

