/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.robots;

import static com.esri.gpt.framework.robots.BotsUtils.decode;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Path.
 */
class AccessPath {
  private final String path;

  /**
   * Creates instance of the path.
   * @param relativePath path relative to the host
   */
  public AccessPath(String relativePath) {
    this.path = relativePath;
  }

  /**
   * Gets path.
   * @return path
   */
  public String getPath() {
    return path;
  }
  
  /**
   * Checks if given path matches.
   * @param relativePath path to check
   * @return <code>true</code> if path matches
   */
  public boolean match(String relativePath) {
    if (relativePath==null) return false;
    if (getPath().isEmpty()) return true;
    
    try {
      relativePath = decode(relativePath);
      Pattern pattern = makePattern(getPath());
      Matcher matcher = pattern.matcher(relativePath);
      return matcher.find() && matcher.start()==0;
    } catch (Exception ex) {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return path;
  }
  
  private Pattern makePattern(String patternWithWildcards) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<patternWithWildcards.length(); i++) {
      char c = patternWithWildcards.charAt(i);
      switch (c) {
        case '*':
          sb.append(".*");
          break;
        case '$':
          if (i==patternWithWildcards.length()-1) {
            sb.append(c);
          } else {
            sb.append("[").append(c).append("]");
          }
          break;
        case '[':
        case ']':
          sb.append("[").append("\\").append(c).append("]");
          break;
        default:
          sb.append("[").append(c).append("]");
      }
    }
    return Pattern.compile(sb.toString(),Pattern.CASE_INSENSITIVE);
  }
}
