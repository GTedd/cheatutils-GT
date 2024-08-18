import { ref, nextTick, onUnmounted } from '/vue.esm-browser.js'
import { addComponent } from '/components/Loader.js'
import { createBlockRenderer, removeBlockRenderer } from './BlockRenderer.js'
import * as http from '/http.js';

let blockInfoPromise = http.get('/api/block-info').then(blocksList => {
    let blocksMap = {};
    blocksList.forEach(b => blocksMap[b.id] = b);
    return {
        blocksList: blocksList,
        blocksMap: blocksMap
    }
});

function createComponent(template) {
    let args = {
        template: template,
        setup() {
            const state = ref('list');
            const search = ref('');
            const blocksList = ref(null);
            const blocksMap = ref(null);
            const blocksConfigList = ref(null);
            const blocksConfigMap = ref(null);
            const selectedConfig = ref(null);
            const blockListFiltered = ref(null);

            const backToList = () => {
                state.value = 'list';
                blocksConfigList.value.forEach(config => {
                    config.expanded = false;
                });

                nextTick(() => setupObserver());
            };

            const backToEdit = () => {
                state.value = 'edit';
            };

            const filterBlockList = () => {
                let searchLower = search.value.toLocaleLowerCase();
                blockListFiltered.value = blocksList.value.filter(block => {
                    if (block.name != null) {
                        let name = block.name.toLocaleLowerCase();
                        if (name.indexOf(searchLower) >= 0) {
                            return true;
                        }
                    }
                    if (block.id != null) {
                        let id = block.id.toLocaleLowerCase();
                        if (id.indexOf(searchLower) >= 0) {
                            return true;
                        }
                    }
                    return false;
                });

                nextTick(() => setupObserver());
            };

            const openAdd = () => {
                state.value = 'add';
                search.value = '';
                filterBlockList();
            };

            const openEdit = id => {
                state.value = 'edit';
                if (blocksConfigMap.value[id]) {
                    selectedConfig.value = blocksConfigMap.value[id];
                    selectedConfig.value.expanded = false;
                } else {
                    selectedConfig.value = null;
                    http.post('/api/blocks-add', id).then(response => {
                        selectedConfig.value = response;
                        blocksConfigList.value.push(selectedConfig.value);
                        blocksConfigMap.value[id] = selectedConfig.value;
                    });
                }
            };

            const editGroup = () => {
                state.value = 'edit-group';
                filterBlockList();
            };

            const remove = () => {
                if (selectedConfig.value) {
                    http.delete('/api/blocks/' + encodeURIComponent(selectedConfig.value.blocks[0])).then(() => {
                        blocksConfigList.value = blocksConfigList.value.filter(c => c != selectedConfig.value);
                        selectedConfig.value.blocks.forEach(id => {
                            delete blocksConfigMap.value[id];
                        })
                        selectedConfig.value = null;
                        backToList();
                    });
                }
            };

            const removeById = id => {
                http.delete('/api/blocks/' + encodeURIComponent(id)).then(() => {
                    blocksConfigList.value = blocksConfigList.value.filter(b => !b.blocks.includes(id));
                    delete blocksConfigMap.value[id];
                });
            };

            const rescan = () => {
                http.post('/api/rescan-chunks');
            };

            const update = config => {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.outlineMaxDistance == '') {
                    config.outlineMaxDistance = null;
                }
                http.post('/api/blocks', config);
            };

            const expandGroup = config => {
                config.expanded = !config.expanded;

                // currentList can't be null here
                const oldItems = [...currentList.children];
                nextTick(() => {
                    for (let item of currentList.children) {
                        if (!oldItems.includes(item)) {
                            observer.observe(item);
                        }
                    }
                });
            };

            const groupEditShouldShowCheckbox = block => {
                return blocksConfigMap.value[block.id] == null || blocksConfigMap.value[block.id] == selectedConfig.value;
            };

            const groupEditGetCheckboxSelected = block => {
                return blocksConfigMap.value[block.id] == selectedConfig.value;
            };

            const groupEditSetCheckboxSelected = (block, event) => {
                if (event.target.checked) {
                    if (!selectedConfig.value.blocks.some(id => id == block.id)) {
                        selectedConfig.value.blocks.push(block.id);
                        blocksConfigMap.value[block.id] = selectedConfig.value;
                        update(selectedConfig.value);
                    }
                } else {
                    selectedConfig.value.blocks = selectedConfig.value.blocks.filter(id => id != block.id);
                    delete blocksConfigMap.value[block.id];
                    update(selectedConfig.value);
                }
            };

            const groupEditIsCheckboxDisabled = block => {
                return selectedConfig.value.blocks.length == 1 && groupEditGetCheckboxSelected(block);
            };

            let observer = null;
            let currentList = null;

            const removeObserver = () => {
                if (observer != null) {
                    observer.disconnect();
                    observer = null;
                }
            };

            const setupObserver = () => {
                removeObserver();

                currentList = document.querySelector('ul.blocks-list, ul.add-block-list');
                if (currentList == null) {
                    console.error('Cannot find list to initialize observer');
                    return;
                }

                observer = new IntersectionObserver(entries => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            let id = entry.target.getAttribute('data-id');
                            createBlockRenderer(entry.target.querySelector('div.c1 > div'), id);
                        } else {
                            removeBlockRenderer(entry.target.querySelector('div.c1 > div'));
                        }
                    });
                  }, {
                    root: currentList,
                    threshold: .5
                });

                for (let item of currentList.children) {
                    observer.observe(item);
                }
            };

            blockInfoPromise.then(info => {
                blocksList.value = info.blocksList;
                blocksMap.value = info.blocksMap;
            });

            http.get('/api/blocks').then(response => {
                blocksConfigList.value = response;

                blocksConfigMap.value = {};
                blocksConfigList.value.forEach(c => {
                    c.blocks.forEach(b => {
                        blocksConfigMap.value[b] = c;
                    });
                });

                nextTick(() => setupObserver());
            });

            onUnmounted(() => {
                removeObserver();
            });

            return {
                state,
                search,
                blocksList,
                blocksMap,
                blocksConfigList,
                blocksConfigMap,
                selectedConfig,
                blockListFiltered,

                backToList,
                backToEdit,
                filterBlockList,
                openAdd,
                openEdit,
                editGroup,
                remove,
                removeById,
                rescan,
                update,
                expandGroup,
                groupEditShouldShowCheckbox,
                groupEditGetCheckboxSelected,
                groupEditSetCheckboxSelected,
                groupEditIsCheckboxDisabled
            };
        }
    };

    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');

    return args;
}

export { createComponent }