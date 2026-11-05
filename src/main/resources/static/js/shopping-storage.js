export function readStoredJson(key) {
    try {
        const value = localStorage.getItem(key);

        return value
            ? JSON.parse(value)
            : null;
    } catch (error) {
        console.error(
            `Could not read ${key} from localStorage`,
            error
        );

        return null;
    }
}

export function writeStoredJson(key, value) {
    localStorage.setItem(
        key,
        JSON.stringify(value)
    );
}

export function removeStoredValue(key) {
    localStorage.removeItem(key);
}

export function saveShoppingSelections(
    shoppingList,
    selectedOptions
) {
    const selections = shoppingList.items.map(
        (item, itemIndex) => ({
            itemIndex: itemIndex,
            productId:
            selectedOptions.get(itemIndex)?.productId
        })
    );

    writeStoredJson(
        "shoppingSelections",
        {
            userId: shoppingList.userId,
            selections: selections
        }
    );
}