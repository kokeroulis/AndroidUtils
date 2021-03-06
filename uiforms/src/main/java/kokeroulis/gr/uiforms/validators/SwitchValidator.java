package kokeroulis.gr.uiforms.validators;

/*  Copyright (C) 2015 Antonis Tsiapaliokas
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    */

public class SwitchValidator implements Validator<Boolean> {
    private ValidatorDelegate<Boolean> mDelegate;

    @Override
    public Boolean getValue() {
        return getDelegate().getValue();
    }

    @Override
    public void setValue(Boolean value) {
        getDelegate().setValue(value);
    }

    @Override
    public boolean isValid() {
        return getValue() == null ? false : getValue();
    }

    public ValidatorDelegate<Boolean> getDelegate() {
        if (mDelegate == null) {
            mDelegate = ValidatorDelegate.create(this);
        }

        return mDelegate;
    }
}
