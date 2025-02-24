import { toRefs } from '/vue.esm-browser.js';

export function createComponent(template) {
    return {
        template,
        props: {
            module: {
                type: Object,
                required: true
            },
            statuses: {
                type: Object,
                required: true
            },
            filtered: {
                type: Object,
                required: true
            }
        },
        setup(props) {
            const onClick = module => {
                window.location.hash = '#/' + module.path;
            };

            return {
                ...toRefs(props),
                onClick
            }
        }
    };
};