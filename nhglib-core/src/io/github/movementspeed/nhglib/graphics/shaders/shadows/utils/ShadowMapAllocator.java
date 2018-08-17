/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.github.movementspeed.nhglib.graphics.shaders.shadows.utils;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

/** Shadow map allocator return texture region for each light
 * @author realitix */
public interface ShadowMapAllocator {

	/** Result of the allocator analyze */
	public class ShadowMapRegion {
		public int x, y, width, height;
	}

	/** Begin the texture allocation */
	public void begin();

	/** End the texture allocation */
	public void end();

	/** Find the next texture region for the current light
	 * @param light Current light
	 * @return ShadowMapRegion or null if no more space on texture */
	public ShadowMapRegion nextResult(BaseLight light);

	/** Return shadow map width.
	 * @return int */
	public int getWidth();

	/** Return shadow map height.
	 * @return int */
	public int getHeight();
}