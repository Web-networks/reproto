Neuroide Reproto
================

This library provides the prototype representation layer for Neuroide. It consists of

* Common part: CRDTs, Prototype, Layer, serialization utilities, etc.
* Client part, which can be run either on JVM or in browser.
* Server part which is supposed to be run on JVM.

Server part of the library is utilized in [reproto-service](https://github.com/Web-networks/reproto-service). This document describes the API mostly from client perspective.

### Build and link

```sh
./gradlew build
```

Npm packages will appear in the folder `build/js`. If you prefer plain JS files, you may copy them to specified directory with

```sh
./copy-scripts.sh path/to/destination
```

### Entities

#### Prototype

Prototype represents a replica of the model. It has the following properties and methods.

| Method/Property                       | Return type | Description                                                  |
| ------------------------------------- | ----------- | ------------------------------------------------------------ |
| `layers`                              | `[Layer]`   | Read-only array of layers.                                   |
| `addLayer(position)`                  | `Layer`     | Create new layer and insert it before the `position`th element. |
| `removeLayer(position)`               | -           | Remove layer at `position`.                                  |
| `moveLayer(fromPosition, toPosition)` | -           | Move layer from `fromPosition` to the place before the element at `toPosition` in source array. |
| `setListeners(listeners)`             | -           | See description of listeners below.                          |

Modification methods (`addLayer`, `removeLayer`, `moveLayer`) are immediately applied to the local copy, trigger listeners, produce update and publish it through Gateway (see below).

#### Layer

Represents one layer of the model. It has only one method.

| Method/Property | Return type       | Description                                                  |
| --------------- | ----------------- | ------------------------------------------------------------ |
| `get(name)`     | `RegisterWrapper` | Returns register for parameter `name`. If the parameter does not exist, it will be created with empty value. |

`get` neither triggers listeners nor produces any updates. New parameters are created just locally. Other replicas won't know about them before you assign a new value to the register.

There is no way to get the list of available parameters as well as to delete a parameter. User code must control the set of necessary parameters itself. It is supposed that the set of ever used parameters is moderately small so the overhead of unused parameters is acceptable.

To summarize, one may assume that all parameters exist by default but they have empty value before some replica changes it.

#### RegisterWrapper

It is a wrapper over last-write-wins register. It provides automatic conversion to some data types.

| Property       | Return type | Default value |
| -------------- | ----------- | ------------- |
| `value`        | `String`    | `""`          |
| `intValue`     | `int`       | `0`           |
| `doubleValue`  | `double`    | `0.0`         |
| `booleanValue` | `boolean`   | `false`       |

Each property is read read/write accessible. Assignment will immediately apply changes to the local copy, trigger listeners, produce update and publish it through Gateway (see below).

Note that the value stored in the register is actually string. Read/write to `intValue` or `booleanValue` just causes the value to be parse/stringified. So if the value cannot be parsed to the corresponding type, an exception will be thrown.

#### Listeners

Listeners is an object which must implement [the interface `PrototypeListener`](https://github.com/Web-networks/reproto/blob/master/src/commonMain/kotlin/PrototypeListener.kt). In terms of JavaScript it means that all methods of the interface must be presented.

| Method                                         | Comment                                                      |
| ---------------------------------------------- | ------------------------------------------------------------ |
| `layerAdded(index, layer)`                     |                                                              |
| `layerRemoved(index, layer)`                   |                                                              |
| `layerMoved(from, to, layer)`                  | `from` an index of the layer in the source array and `to` is an index in the resulting array of layers. |
| `parameterChanged(layer, paramName, register)` | `register` is `RegisterWrapper`.                             |

Note that currently indices are not computed correctly but it is going to be fixed soon.

### Node

Nodes are used to access prototypes. There are two kinds of nodes: `ServiceNode` and `ClientNode`. Here the latter is described.

#### Construction

```js
let rp = reproto.raid.neuroide.reproto; // shortcut
let node = new rp.ClientNode(siteId);
```

Here `siteId` is a string identifier. It must be unique across all nodes. It is recommended to preserve this id between restarts of the same node for performance reasons. For example, if a user exits his account and enters it again, the node can and should be created with the same id. However, if the user opens the second tab with the application, the node must be created with another id to preserve uniqueness.

Node identifiers are saved in vector clocks to maintain prototype revisions. So the more unique identifiers are used the larger vector clocks will be transferred. Garbage collection has not been implemented yet.

#### Access prototypes

Client node can keep in memory one prototype at a time.

| Method                       | Description                                                  |
| ---------------------------- | ------------------------------------------------------------ |
| `getPrototype(id, callback)` | If current prototype id is not equal to `id`, load specified prototype and make it current. Then call `callback(current prototype)`. |
| `setGateway(gateway)`        | See below.                                                   |

#### Gateway

Client node interacts with outer world through gateway. Gateway must implement [interface `ClientGateway`](https://github.com/Web-networks/reproto/blob/master/src/commonMain/kotlin/ClientGateway.kt) and have the following methods.

| Method                      | Description                                                  |      |
| --------------------------- | ------------------------------------------------------------ | ---- |
| `load(id)`                  | Node calls this method when it wants to download prototype.  |      |
| `setReceiver(receiver)`     | Set prototype receiver. When new prototype is loaded, gateway must call `receiver(id, prototype)`, where `prototype` is string. |      |
| `subscribe(processor)`      | Set update processor. When new update is received, gateway must call `processor(update)`, where update is string. |      |
| `publishUpdate(update)`     | Send new locally issued update to server.                    |      |
| `requestSync(id, revision)` | This method is called each time the prototype revision is changed. |      |

### Technical details

If JS modules are used, all symbols are accessible as properties of the object `moduleName.raid.neuroide.reproto`, else as properties of `reproto.raid.neuroide.reproto`.

### See also

- [reproto-service](https://github.com/Web-networks/reproto-service)
- [Design Document](https://docs.google.com/document/d/1cHbbvcdDRKtzS8CAxQvWhC8s5-DHV13p52g1_WXXRvo/edit?usp=sharing)
