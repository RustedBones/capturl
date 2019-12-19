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

import fr.davit.capturl.scaladsl.Iri$;
import fr.davit.capturl.scaladsl.Iri.ParsingMode$;

public abstract class Iri {

    public static Iri create(String iri, String mode) { return Iri$.MODULE$.apply(iri, ParsingMode$.MODULE$.apply(mode)); }

    public static Iri create(String iri) { return create(iri, "strict"); }

    public static final Iri EMPTY = fr.davit.capturl.scaladsl.Iri.EMPTY;

    //------------------------------------------------------------------------------------------------------------------
    // Iri
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isValid();

    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

    public abstract Iri relativize(Iri iri);

    public abstract Iri resolve(Iri iri);

    public abstract fr.davit.capturl.scaladsl.Iri asScala();


    //------------------------------------------------------------------------------------------------------------------
    // Scheme
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isSchemeValid();

    public abstract String getScheme();

    public abstract String getRawScheme();

    public abstract Iri withScheme(String scheme);

    //------------------------------------------------------------------------------------------------------------------
    // Authority
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isAuthorityValid();

    public abstract Authority getAuthority();

    public abstract String getRawAuthority();

    public abstract Iri withAuthority(Authority authority);

    public abstract Iri withAuthority(String authority);

    //------------------------------------------------------------------------------------------------------------------
    // Path
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isPathValid();

    public abstract Path getPath();

    public abstract String getRawPath();

    public abstract Iri withPath(Path path);

    public abstract Iri withPath(String path);


    //------------------------------------------------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isQueryValid();

    public abstract Query getQuery();

    public abstract String getRawQuery();

    public abstract Iri withQuery(Query query);

    public abstract Iri withQuery(String query);

    //------------------------------------------------------------------------------------------------------------------
    // Fragment
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isFragmentValid();

    public abstract String getFragment();

    public abstract String getRawFragment();

    public abstract Iri withFragment(String fragment);

}
