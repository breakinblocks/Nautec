---
navigation:
  title: Resonance Network
  icon: nautec:resonance_pylon
  position: 9
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:resonance_pylon
  - nautec:abyssal_pylon
  - nautec:prismatic_emitter
  - nautec:tuning_fork
  - nautec:resonance_charm
---

# <Color id="light_purple">Resonance Network</Color>

<Column alignItems="center" fullWidth={true}>
  <Row>
    <ItemImage id="resonance_pylon" scale="2"/>
    <ItemImage id="abyssal_pylon" scale="2"/>
  </Row>
  Crystal pylons that carry Forge Energy (FE) between your bases without cables.
</Column>

A Resonance Network is a named grid of pylons. Every pylon on the same network shares FE with the others, however far apart they are, with no cables and no line of sight.

***

## <Color id="gold">Setting Up</Color>

1. Place a <ItemLink id="resonance_pylon"/> and right-click it with an empty hand.
2. Type a name in the box and press **Create**. The network is yours, and the pylon joins it.
3. Place another pylon at the other site, open it, and use the arrows to pick the same network.
4. Set each pylon to **Sending** or **Receiving** with the button at the top right.

A sending pylon takes FE from cables or machines on any side and sends it into the network. A receiving pylon takes FE from the network and pushes it into anything touching it. A network can have many of each.

***

## <Color id="gold">Range and Loss</Color>

| | Carries | Holds | Reaches |
|---|---|---|---|
| <ItemLink id="resonance_pylon"/> | 20,000 FE/t | 200,000 FE | Any pylon in the same dimension, losing 5% |
| <ItemLink id="abyssal_pylon"/> | 50,000 FE/t | 500,000 FE | The same, plus other Abyssal Pylons in any dimension, losing 15% |

FE only crosses dimensions between two Abyssal Pylons, one at each end. A receiver takes from senders in its own dimension first, since that loses less.

Pylons only work while their chunk is loaded, so keep both ends of a long link chunk loaded if it has to run while you are away.

***

## <Color id="gold">Who Can Use It</Color>

Only the owner and the players they trust can link a pylon to a network or open a pylon that is already on it. Anyone else gets a message naming the network and its owner.

The owner manages access from any pylon on the network:

* **Trust**: type the name of an online player and press Trust. They can now link pylons to the network. Remove them with the red x beside their name.
* **Team**: with FTB Teams installed, turning this on lets everyone in the owner's team use the network without trusting each of them.
* **Delete**: removes the network and unlinks every pylon on it. Click it twice.

Each player can own up to 16 networks.

***

<Row>
  <ItemImage id="prismatic_emitter"/>
  ### <Color id="aqua">Prismatic Emitter</Color>
</Row>

Powers nearby machines without cables. Feed it FE from a cable or a receiving pylon placed against it, then link the machines it should power. It fires a crystal tether to each one and shares up to 10,000 FE/t between them, with nothing lost on the way.

1. Hold a <ItemLink id="tuning_fork"/> and right-click the emitter to tune the fork to it.
2. Right-click each machine to link it. Right-click a linked machine again to unlink it.

An emitter reaches machines up to 16 blocks away and can feed 16 of them. Only the player who placed it can tune a fork to it. Sneak-right-click the emitter with the fork to unlink everything, or sneak-right-click the air to clear the fork.

***

<Row>
  <ItemImage id="resonance_charm"/>
  ### <Color id="aqua">Resonance Charm</Color>
</Row>

Charges the items you carry from a network, wherever you are.

Right-click a pylon with the charm to bind it to that pylon's network; you need access to the network. Then wear it in a charm slot. Every half second it draws from the network's sending pylons and charges the FE items in your inventory, armour and curio slots, along with NauTec items that run on laser power, such as the <ItemLink id="atlantean_rifle"/> and the <ItemLink id="prismatic_battery"/>. It moves up to 2,000 FE/t.

Its crystal also does the work of a <ItemLink id="prism_monocle"/>: while it is equipped you see power and purity readouts on laser blocks and the clearer underwater view without wearing a monocle.

It works in any dimension where the network has a sending pylon, losing 5% like a pylon link. In other dimensions it can still draw from the network's Abyssal Pylons, losing 15%. If the owner stops trusting you, the charm stops working until you are trusted again. Sneak-right-click the air with it to unbind it.
