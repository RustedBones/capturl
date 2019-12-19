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

import fr.davit.capturl.scaladsl.Authority$;

import java.util.Optional;

public abstract class Authority {

    public static Authority create(String authority) { return Authority$.MODULE$.apply(authority); }

    //------------------------------------------------------------------------------------------------------------------
    // Authority
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract fr.davit.capturl.scaladsl.Authority asScala();

    //------------------------------------------------------------------------------------------------------------------
    // Host
    //------------------------------------------------------------------------------------------------------------------
    public abstract Host getHost();

    public abstract Authority withHost(String host);

    //------------------------------------------------------------------------------------------------------------------
    // Port
    //------------------------------------------------------------------------------------------------------------------
    public abstract Optional<Integer> getPort();

    public abstract Authority withPort(int port);

    public abstract Optional<String> getUserInfo();

    public abstract Authority withUserInfo(String userInfo);
}
