import { addComponent } from '/components/Loader.js'
import { handleCodeSave } from '/components/MonacoEditor.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            refresh() {
                let self = this;
                axios.get('/api/status-overlay').then(function (response) {
                    self.config = response.data;
                });
            },
            save() {
                handleCodeSave('/api/status-overlay-code', this.config.code);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        let self = this;
                        axios.get('/api/scripts-doc/OVERLAY').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            update() {
                let self = this;
                axios.post('/api/status-overlay', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }