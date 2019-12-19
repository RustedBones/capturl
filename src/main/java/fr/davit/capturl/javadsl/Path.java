/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Path$;

public abstract class Path {

    public static Path create(String path) { return Path$.MODULE$.apply(path); }

    //------------------------------------------------------------------------------------------------------------------
    // Path
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract int length();

    public abstract boolean startsWithSlash();

    public abstract boolean startsWithSegment();

    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

    public abstract Path relativize(Path path);

    public abstract Path resolve(Path path);

    public abstract Path normalize();

    public abstract Path appendSlash();

    public abstract Path appendSegment(String segment);

    public abstract Iterable<String> getSegments();

    public abstract fr.davit.capturl.scaladsl.Path asScala();

}
